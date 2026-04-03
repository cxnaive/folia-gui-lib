package com.thenextlvl.foliagui.manager;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.input.ChatInputRequest;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 聊天输入管理器
 * <p>
 * 管理玩家的聊天输入请求，支持：
 * - 关闭现有GUI并发送提示
 * - 捕获玩家下一次聊天消息
 * - 恢复之前的GUI
 * - 超时处理
 * - 取消关键词
 *
 * @author TheNextLvl
 */
public class ChatInputManager implements Listener {

    private static ChatInputManager instance;
    private final Plugin plugin;
    private final Map<UUID, ActiveRequest> activeRequests = new ConcurrentHashMap<>();
    private final Map<UUID, GUIContext> savedGUIs = new ConcurrentHashMap<>();

    private ChatInputManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 初始化聊天输入管理器
     * @param plugin 插件实例
     * @return 管理器实例
     */
    public static synchronized ChatInputManager init(@NotNull Plugin plugin) {
        if (instance == null) {
            instance = new ChatInputManager(plugin);
        }
        return instance;
    }

    /**
     * 获取管理器实例
     * @return 实例，如果未初始化则返回null
     */
    public static @Nullable ChatInputManager getInstance() {
        return instance;
    }

    /**
     * 检查玩家是否有待处理的输入请求
     * @param player 玩家
     * @return 是否有待处理请求
     */
    public boolean hasPendingRequest(@NotNull Player player) {
        return activeRequests.containsKey(player.getUniqueId());
    }

    /**
     * 获取玩家当前的输入请求
     * @param player 玩家
     * @return 输入请求，如果没有则返回null
     */
    public @Nullable ChatInputRequest getPendingRequest(@NotNull Player player) {
        ActiveRequest request = activeRequests.get(player.getUniqueId());
        return request != null ? request.request : null;
    }

    /**
     * 创建输入请求构建器
     * @param player 目标玩家
     * @return 构建器
     */
    @NotNull
    public ChatInputRequest.Builder requestInput(@NotNull Player player) {
        return new InputRequestBuilder(player);
    }

    /**
     * 快捷方法：请求玩家输入
     * @param player 玩家
     * @param prompt 提示消息
     * @param callback 输入完成回调
     * @return 输入请求
     */
    @NotNull
    public ChatInputRequest requestInput(@NotNull Player player, @NotNull String prompt,
                                          @NotNull Consumer<ChatInputRequest.InputResult> callback) {
        return requestInput(player)
                .prompt(prompt)
                .onComplete(callback)
                .submit();
    }

    /**
     * 快捷方法：请求玩家输入并恢复GUI
     * @param player 玩家
     * @param prompt 提示消息
     * @param guiId 要恢复的GUI ID
     * @param callback 输入完成回调
     * @return 输入请求
     */
    @NotNull
    public ChatInputRequest requestInputAndRestore(@NotNull Player player, @NotNull String prompt,
                                                    @NotNull String guiId,
                                                    @NotNull Consumer<ChatInputRequest.InputResult> callback) {
        return requestInput(player)
                .prompt(prompt)
                .restoreGUI(true)
                .onComplete(result -> {
                    callback.accept(result);
                    if (!result.isCancelled() && !result.isTimedOut()) {
                        FoliaScheduler.runOnPlayer(player, () -> {
                            GUIManager guiManager = GUIManager.getInstance();
                            if (guiManager != null) {
                                guiManager.open(player, guiId);
                            }
                        });
                    }
                })
                .submit();
    }

    /**
     * 取消玩家的输入请求
     * @param player 玩家
     * @return 是否成功取消
     */
    public boolean cancelRequest(@NotNull Player player) {
        ActiveRequest request = activeRequests.remove(player.getUniqueId());
        if (request != null) {
            request.cancel();
            return true;
        }
        return false;
    }

    /**
     * 保存GUI上下文（用于后续恢复）
     * @param player 玩家
     * @param gui GUI实例
     */
    public void saveGUIContext(@NotNull Player player, @NotNull GUI gui) {
        savedGUIs.put(player.getUniqueId(), new GUIContext(gui, System.currentTimeMillis()));
    }

    /**
     * 恢复玩家之前保存的GUI
     * @param player 玩家
     * @return 是否成功恢复
     */
    public boolean restoreGUI(@NotNull Player player) {
        GUIContext context = savedGUIs.remove(player.getUniqueId());
        if (context != null) {
            GUIManager guiManager = GUIManager.getInstance();
            if (guiManager != null) {
                FoliaScheduler.runOnPlayer(player, () -> {
                    guiManager.open(player, context.gui);
                });
                return true;
            }
        }
        return false;
    }

    // ==================== 事件处理 ====================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ActiveRequest activeRequest = activeRequests.get(player.getUniqueId());

        if (activeRequest == null) {
            return;
        }

        // 取消聊天事件，防止其他插件处理
        event.setCancelled(true);

        String message = event.getMessage();
        ChatInputRequestImpl request = activeRequest.request;

        // 检查取消关键词
        for (String keyword : request.getCancelKeywords()) {
            if (message.equalsIgnoreCase(keyword)) {
                handleCancel(player, activeRequest);
                return;
            }
        }

        // 验证输入
        if (request.getValidator() != null && !request.getValidator().test(message)) {
            String errorMsg = request.getValidationErrorMessage();
            if (errorMsg != null) {
                player.sendMessage(errorMsg.replace('&', '§'));
            }
            // 不结束请求，让玩家重新输入
            return;
        }

        // 成功获取输入
        activeRequests.remove(player.getUniqueId());

        if (activeRequest.timeoutTask != null) {
            activeRequest.timeoutTask.cancel();
        }

        // 在主线程处理结果
        FoliaScheduler.runOnPlayer(player, () -> {
            ChatInputRequest.InputResult result = new ChatInputRequest.InputResult(
                    message, false, false, player
            );

            if (request.getOnComplete() != null) {
                request.getOnComplete().accept(result);
            }

            // 恢复GUI
            if (request.isRestoreGUI()) {
                restoreGUI(player);
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        ActiveRequest request = activeRequests.remove(playerId);
        if (request != null && request.timeoutTask != null) {
            request.timeoutTask.cancel();
        }
        savedGUIs.remove(playerId);
    }

    private void handleCancel(Player player, ActiveRequest activeRequest) {
        activeRequests.remove(player.getUniqueId());

        if (activeRequest.timeoutTask != null) {
            activeRequest.timeoutTask.cancel();
        }

        ChatInputRequestImpl request = activeRequest.request;
        request.cancelled = true;

        player.sendMessage("§c输入已取消。");

        if (request.getOnCancel() != null) {
            request.getOnCancel().run();
        }

        // 恢复GUI
        if (request.isRestoreGUI()) {
            restoreGUI(player);
        }
    }

    private void handleTimeout(Player player, ActiveRequest activeRequest) {
        activeRequests.remove(player.getUniqueId());

        ChatInputRequestImpl request = activeRequest.request;
        request.timedOut = true;

        player.sendMessage("§c输入超时，请重新尝试。");

        if (request.getOnTimeout() != null) {
            request.getOnTimeout().run();
        }

        // 恢复GUI
        if (request.isRestoreGUI()) {
            restoreGUI(player);
        }
    }

    // ==================== 内部类 ====================

    private static class ActiveRequest {
        final ChatInputRequestImpl request;
        ScheduledTask timeoutTask;

        ActiveRequest(ChatInputRequestImpl request) {
            this.request = request;
        }

        void cancel() {
            if (timeoutTask != null) {
                timeoutTask.cancel();
            }
        }
    }

    private static class GUIContext {
        final GUI gui;
        final long savedTime;

        GUIContext(GUI gui, long savedTime) {
            this.gui = gui;
            this.savedTime = savedTime;
        }
    }

    private class InputRequestBuilder implements ChatInputRequest.Builder {
        private final Player player;
        private String prompt = "§e请在聊天栏输入内容，输入 'cancel' 取消：";
        private String[] cancelKeywords = {"cancel", "取消"};
        private long timeout = 60000; // 默认60秒超时
        private Predicate<String> validator;
        private String validationErrorMessage = "§c输入无效，请重新输入：";
        private boolean restoreGUI = true;
        private Consumer<ChatInputRequest.InputResult> onComplete;
        private Runnable onCancel;
        private Runnable onTimeout;

        InputRequestBuilder(Player player) {
            this.player = player;
        }

        @Override
        public @NotNull ChatInputRequest.Builder prompt(@NotNull String prompt) {
            this.prompt = prompt;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder cancelKeywords(@NotNull String... keywords) {
            this.cancelKeywords = keywords;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder timeout(long milliseconds) {
            this.timeout = milliseconds;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder validator(@NotNull Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder validationErrorMessage(@NotNull String message) {
            this.validationErrorMessage = message;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder restoreGUI(boolean restore) {
            this.restoreGUI = restore;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder onComplete(@NotNull Consumer<ChatInputRequest.InputResult> callback) {
            this.onComplete = callback;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder onCancel(@NotNull Runnable callback) {
            this.onCancel = callback;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest.Builder onTimeout(@NotNull Runnable callback) {
            this.onTimeout = callback;
            return this;
        }

        @Override
        public @NotNull ChatInputRequest submit() {
            // 如果已有待处理请求，先取消
            ActiveRequest existing = activeRequests.get(player.getUniqueId());
            if (existing != null) {
                existing.cancel();
                activeRequests.remove(player.getUniqueId());
            }

            // 保存当前GUI
            if (restoreGUI) {
                GUIManager guiManager = GUIManager.getInstance();
                if (guiManager != null) {
                    GUI currentGUI = guiManager.getOpenGUI(player);
                    if (currentGUI != null) {
                        savedGUIs.put(player.getUniqueId(), new GUIContext(currentGUI, System.currentTimeMillis()));
                        player.closeInventory();
                    }
                }
            }

            // 创建请求
            ChatInputRequestImpl request = new ChatInputRequestImpl(
                    UUID.randomUUID().toString(),
                    player,
                    prompt,
                    cancelKeywords,
                    timeout,
                    validator,
                    validationErrorMessage,
                    restoreGUI,
                    onComplete,
                    onCancel,
                    onTimeout
            );

            ActiveRequest activeRequest = new ActiveRequest(request);

            // 设置超时
            if (timeout > 0) {
                activeRequest.timeoutTask = FoliaScheduler.runLaterAsync(timeout, task -> {
                    if (activeRequests.containsKey(player.getUniqueId())) {
                        handleTimeout(player, activeRequest);
                    }
                });
            }

            activeRequests.put(player.getUniqueId(), activeRequest);

            // 发送提示消息
            player.sendMessage(prompt.replace('&', '§'));
            if (cancelKeywords.length > 0) {
                player.sendMessage("§7输入 §f" + cancelKeywords[0] + " §7取消输入。");
            }

            return request;
        }
    }

    private static class ChatInputRequestImpl implements ChatInputRequest {
        private final String id;
        private final Player player;
        private final String prompt;
        private final String[] cancelKeywords;
        private final long timeout;
        private final long createTime;
        private final Predicate<String> validator;
        private final String validationErrorMessage;
        private final boolean restoreGUI;
        private final Consumer<InputResult> onComplete;
        private final Runnable onCancel;
        private final Runnable onTimeout;

        private volatile boolean cancelled = false;
        private volatile boolean timedOut = false;

        ChatInputRequestImpl(String id, Player player, String prompt, String[] cancelKeywords,
                             long timeout, Predicate<String> validator, String validationErrorMessage,
                             boolean restoreGUI, Consumer<InputResult> onComplete,
                             Runnable onCancel, Runnable onTimeout) {
            this.id = id;
            this.player = player;
            this.prompt = prompt;
            this.cancelKeywords = cancelKeywords;
            this.timeout = timeout;
            this.createTime = System.currentTimeMillis();
            this.validator = validator;
            this.validationErrorMessage = validationErrorMessage;
            this.restoreGUI = restoreGUI;
            this.onComplete = onComplete;
            this.onCancel = onCancel;
            this.onTimeout = onTimeout;
        }

        @Override
        public @NotNull String getId() {
            return id;
        }

        @Override
        public @NotNull Player getPlayer() {
            return player;
        }

        @Override
        public @NotNull String getPrompt() {
            return prompt;
        }

        @Override
        public @NotNull String[] getCancelKeywords() {
            return cancelKeywords;
        }

        @Override
        public boolean isCancelled() {
            return cancelled;
        }

        @Override
        public void cancel() {
            this.cancelled = true;
        }

        @Override
        public long getTimeout() {
            return timeout;
        }

        @Override
        public long getRemainingTime() {
            if (timeout <= 0) return -1;
            long elapsed = System.currentTimeMillis() - createTime;
            long remaining = timeout - elapsed;
            return Math.max(0, remaining);
        }

        public Predicate<String> getValidator() {
            return validator;
        }

        public String getValidationErrorMessage() {
            return validationErrorMessage;
        }

        public boolean isRestoreGUI() {
            return restoreGUI;
        }

        public Consumer<InputResult> getOnComplete() {
            return onComplete;
        }

        public Runnable getOnCancel() {
            return onCancel;
        }

        public Runnable getOnTimeout() {
            return onTimeout;
        }
    }
}
