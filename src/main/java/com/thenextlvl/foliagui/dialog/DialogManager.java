package com.thenextlvl.foliagui.dialog;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.dialog.ConfirmDialogConfig;
import com.thenextlvl.foliagui.dialog.InputDialogConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 对话框管理器
 * <p>
 * 管理确认对话框和输入对话框
 *
 * @author TheNextLvl
 */
public class DialogManager {

    private final Plugin plugin;
    private final Map<UUID, DialogSession> activeSessions = new ConcurrentHashMap<>();

    // 对话框配置提供器
    private final Map<String, Function<Player, ConfirmDialogConfig>> confirmDialogProviders = new ConcurrentHashMap<>();
    private final Map<String, Function<Player, InputDialogConfig>> inputDialogProviders = new ConcurrentHashMap<>();

    public DialogManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 注册确认对话框配置提供器
     *
     * @param id       对话框 ID
     * @param provider 配置提供器
     */
    public void registerConfirmDialog(@NotNull String id,
                                       @NotNull Function<Player, ConfirmDialogConfig> provider) {
        confirmDialogProviders.put(id.toLowerCase(), provider);
    }

    /**
     * 注册输入对话框配置提供器
     *
     * @param id       对话框 ID
     * @param provider 配置提供器
     */
    public void registerInputDialog(@NotNull String id,
                                     @NotNull Function<Player, InputDialogConfig> provider) {
        inputDialogProviders.put(id.toLowerCase(), provider);
    }

    /**
     * 打开确认对话框
     *
     * @param player 玩家
     * @param id     对话框 ID
     * @return 是否成功打开
     */
    public boolean openConfirmDialog(@NotNull Player player, @NotNull String id) {
        Function<Player, ConfirmDialogConfig> provider = confirmDialogProviders.get(id.toLowerCase());
        if (provider == null) {
            return false;
        }

        ConfirmDialogConfig config = provider.apply(player);
        if (config == null) {
            return false;
        }

        showConfirmDialog(player, config);
        return true;
    }

    /**
     * 打开输入对话框
     *
     * @param player 玩家
     * @param id     对话框 ID
     * @return 是否成功打开
     */
    public boolean openInputDialog(@NotNull Player player, @NotNull String id) {
        Function<Player, InputDialogConfig> provider = inputDialogProviders.get(id.toLowerCase());
        if (provider == null) {
            return false;
        }

        InputDialogConfig config = provider.apply(player);
        if (config == null) {
            return false;
        }

        showInputDialog(player, config);
        return true;
    }

    /**
     * 显示确认对话框
     *
     * @param player 玩家
     * @param config 配置
     */
    public void showConfirmDialog(@NotNull Player player, @NotNull ConfirmDialogConfig config) {
        // 创建确认对话框 GUI
        GUIBuilder builder = GUIBuilder.create("confirm-dialog-" + UUID.randomUUID())
                .title(config.getTitle())
                .rows(3);

        // 消息区域（中间行）
        String[] lines = config.getMessage().split("\n");
        int messageStartSlot = 4 - Math.min(lines.length, 3) / 2;

        // 确认按钮（左边）
        builder.button(12, ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                .name(config.getConfirmText())
                .build(), event -> {
            event.setCancelled(true);
            if (config.getOnConfirm() != null) {
                config.getOnConfirm().accept(player);
            }
            if (config.isCloseOnConfirm()) {
                player.closeInventory();
            }
            if (config.getConfirmSound() != null) {
                player.playSound(player.getLocation(), config.getConfirmSound(), 1.0f, 1.0f);
            }
        });

        // 取消按钮（右边）
        builder.button(14, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                .name(config.getCancelText())
                .build(), event -> {
            event.setCancelled(true);
            if (config.getOnCancel() != null) {
                config.getOnCancel().accept(player);
            }
            if (config.isCloseOnCancel()) {
                player.closeInventory();
            }
            if (config.getCancelSound() != null) {
                player.playSound(player.getLocation(), config.getCancelSound(), 1.0f, 1.0f);
            }
        });

        // 边框
        builder.border(Material.GRAY_STAINED_GLASS_PANE);

        GUI dialogGUI = builder.build();
        dialogGUI.open(player);

        // 记录会话
        activeSessions.put(player.getUniqueId(), new DialogSession(player, dialogGUI, "confirm"));
    }

    /**
     * 显示输入对话框
     * <p>
     * 使用聊天输入方式
     *
     * @param player 玩家
     * @param config 配置
     */
    public void showInputDialog(@NotNull Player player, @NotNull InputDialogConfig config) {
        // 关闭当前 GUI
        player.closeInventory();

        // 发送提示
        player.sendMessage(config.getTitle().replace('&', '§'));
        player.sendMessage(config.getPrompt().replace('&', '§'));

        if (config.getDefaultValue() != null && !config.getDefaultValue().isEmpty()) {
            player.sendMessage("§7默认值: " + config.getDefaultValue());
        }

        player.sendMessage("§7输入 'cancel' 或 '取消' 来取消");

        // 记录会话（等待聊天输入）
        activeSessions.put(player.getUniqueId(), new DialogSession(player, null, "input", config));

        // 注册聊天监听器需要在外部处理
        // 这里只创建会话，实际的输入处理需要 ChatInputManager
    }

    /**
     * 处理聊天输入
     *
     * @param player 玩家
     * @param input  输入内容
     * @return 是否处理了输入
     */
    public boolean handleChatInput(@NotNull Player player, @NotNull String input) {
        DialogSession session = activeSessions.get(player.getUniqueId());
        if (session == null || !"input".equals(session.getType())) {
            return false;
        }

        InputDialogConfig config = session.getInputConfig();
        if (config == null) {
            return false;
        }

        // 检查取消关键词
        for (String cancel : new String[]{"cancel", "取消"}) {
            if (input.equalsIgnoreCase(cancel)) {
                if (config.getOnCancel() != null) {
                    config.getOnCancel().accept(player);
                }
                activeSessions.remove(player.getUniqueId());
                return true;
            }
        }

        // 验证输入
        if (!config.validate(input)) {
            player.sendMessage(config.getValidationErrorMessage().replace('&', '§'));
            if (config.getErrorSound() != null) {
                player.playSound(player.getLocation(), config.getErrorSound(), 1.0f, 1.0f);
            }
            return true; // 继续等待有效输入
        }

        // 执行回调
        if (config.getOnSubmit() != null) {
            config.getOnSubmit().accept(input);
        }

        if (config.getSuccessSound() != null) {
            player.playSound(player.getLocation(), config.getSuccessSound(), 1.0f, 1.0f);
        }

        // 清除会话
        activeSessions.remove(player.getUniqueId());

        // 恢复 GUI
        if (config.isRestoreGUI()) {
            // TODO: 恢复之前的 GUI
        }

        return true;
    }

    /**
     * 检查玩家是否有活动的对话框会话
     *
     * @param player 玩家
     * @return 是否有活动会话
     */
    public boolean hasActiveSession(@NotNull Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }

    /**
     * 获取玩家的对话框会话
     *
     * @param player 玩家
     * @return 会话，如果没有则返回 null
     */
    @Nullable
    public DialogSession getSession(@NotNull Player player) {
        return activeSessions.get(player.getUniqueId());
    }

    /**
     * 结束玩家的对话框会话
     *
     * @param player 玩家
     */
    public void endSession(@NotNull Player player) {
        DialogSession session = activeSessions.remove(player.getUniqueId());
        if (session != null) {
            session.cleanup();
        }
    }

    /**
     * 清理所有会话
     */
    public void cleanup() {
        for (DialogSession session : activeSessions.values()) {
            session.cleanup();
        }
        activeSessions.clear();
    }

    /**
     * 对话框会话
     */
    public static class DialogSession {
        private final Player player;
        private final GUI dialogGUI;
        private final String type;
        private final InputDialogConfig inputConfig;
        private final long createdAt;

        public DialogSession(@NotNull Player player, @Nullable GUI dialogGUI, @NotNull String type) {
            this(player, dialogGUI, type, null);
        }

        public DialogSession(@NotNull Player player, @Nullable GUI dialogGUI,
                             @NotNull String type, @Nullable InputDialogConfig inputConfig) {
            this.player = player;
            this.dialogGUI = dialogGUI;
            this.type = type;
            this.inputConfig = inputConfig;
            this.createdAt = System.currentTimeMillis();
        }

        public Player getPlayer() {
            return player;
        }

        public GUI getDialogGUI() {
            return dialogGUI;
        }

        public String getType() {
            return type;
        }

        public InputDialogConfig getInputConfig() {
            return inputConfig;
        }

        public long getCreatedAt() {
            return createdAt;
        }

        public void cleanup() {
            // 清理资源
        }
    }
}