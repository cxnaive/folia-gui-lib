package com.thenextlvl.foliagui.manager;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 告示牌输入管理器
 * <p>
 * 使用 Paper 1.20+ 的 Sign API 实现告示牌输入功能
 * 支持玩家通过告示牌界面输入文本
 *
 * @author TheNextLvl
 */
public class SignInputManager implements Listener {

    private static SignInputManager instance;
    private final Plugin plugin;

    // 存储等待输入的请求
    private final Map<UUID, SignInputRequest> pendingRequests = new ConcurrentHashMap<>();

    // 存储玩家打开GUI前所在的方块（用于恢复）
    private final Map<UUID, BlockState> originalBlocks = new ConcurrentHashMap<>();

    private SignInputManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * 初始化告示牌输入管理器
     * @param plugin 插件实例
     * @return 管理器实例
     */
    public static synchronized SignInputManager init(@NotNull Plugin plugin) {
        if (instance == null) {
            instance = new SignInputManager(plugin);
        }
        return instance;
    }

    /**
     * 获取管理器实例
     * @return 实例，如果未初始化则返回null
     */
    public static @Nullable SignInputManager getInstance() {
        return instance;
    }

    /**
     * 检查玩家是否有待处理的输入请求
     * @param player 玩家
     * @return 是否有待处理请求
     */
    public boolean hasPendingRequest(@NotNull Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    /**
     * 请求玩家通过告示牌输入
     *
     * @param player 玩家
     * @param prompt 提示消息（显示在告示牌上）
     * @param callback 输入完成回调
     */
    public void requestSignInput(@NotNull Player player, @NotNull String prompt,
                                  @NotNull Consumer<SignInputResult> callback) {
        requestSignInput(player, new String[]{prompt, "", "", ""}, callback);
    }

    /**
     * 请求玩家通过告示牌输入
     *
     * @param player 玩家
     * @param lines 告示牌四行内容（第一行通常是提示）
     * @param callback 输入完成回调
     */
    public void requestSignInput(@NotNull Player player, @NotNull String[] lines,
                                  @NotNull Consumer<SignInputResult> callback) {
        requestSignInput(player, lines, null, callback);
    }

    /**
     * 请求玩家通过告示牌输入（支持恢复GUI）
     *
     * @param player 玩家
     * @param lines 告示牌四行内容
     * @param guiToRestore 输入完成后要恢复的GUI（可为null）
     * @param callback 输入完成回调
     */
    public void requestSignInput(@NotNull Player player, @NotNull String[] lines,
                                  @Nullable GUI guiToRestore,
                                  @NotNull Consumer<SignInputResult> callback) {
        // 取消之前的请求
        cancelRequest(player);

        // 在玩家脚下的位置创建虚拟告示牌
        Location signLocation = player.getLocation().clone().subtract(0, 2, 0);

        // 确保位置在加载的区块中
        if (!signLocation.getWorld().isChunkLoaded(signLocation.getBlockX() >> 4, signLocation.getBlockZ() >> 4)) {
            signLocation.getWorld().loadChunk(signLocation.getBlockX() >> 4, signLocation.getBlockZ() >> 4);
        }

        Block block = signLocation.getBlock();
        BlockData originalData = block.getBlockData();

        // 保存原始方块状态
        originalBlocks.put(player.getUniqueId(), new BlockState(block.getType(), originalData));

        // 设置告示牌方块
        block.setType(Material.OAK_SIGN);
        Sign sign = (Sign) block.getState();

        // 设置告示牌内容
        for (int i = 0; i < Math.min(4, lines.length); i++) {
            String line = lines[i].replace('&', '§');
            sign.line(i, Component.text(line));
        }
        sign.update(true);

        // 创建请求
        SignInputRequest request = new SignInputRequest(
                player,
                signLocation,
                guiToRestore,
                callback
        );
        pendingRequests.put(player.getUniqueId(), request);

        // 在主线程打开告示牌编辑界面
        FoliaScheduler.runOnPlayer(player, () -> {
            player.openSign(sign);
        });
    }

    /**
     * 取消玩家的输入请求
     * @param player 玩家
     */
    public void cancelRequest(@NotNull Player player) {
        SignInputRequest request = pendingRequests.remove(player.getUniqueId());
        if (request != null) {
            restoreBlock(player);
        }
    }

    /**
     * 恢复原始方块
     */
    private void restoreBlock(@NotNull Player player) {
        BlockState state = originalBlocks.remove(player.getUniqueId());
        if (state != null) {
            FoliaScheduler.runAtLocation(state.location, () -> {
                state.block.setType(state.material);
                state.block.setBlockData(state.blockData);
            });
        }
    }

    // ==================== 事件处理 ====================

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        SignInputRequest request = pendingRequests.get(player.getUniqueId());

        if (request == null) {
            return;
        }

        // 取消事件传播
        event.setCancelled(true);

        // 移除请求
        pendingRequests.remove(player.getUniqueId());

        // 收集输入的文本
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            net.kyori.adventure.text.Component lineComp = event.line(i);
            String line = net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().serialize(lineComp);
            if (!line.isEmpty()) {
                if (textBuilder.length() > 0) {
                    textBuilder.append("\n");
                }
                textBuilder.append(line);
            }
        }
        String text = textBuilder.toString();

        // 恢复原始方块
        restoreBlock(player);

        // 创建结果
        SignInputResult result = new SignInputResult(text, false, player);

        // 触发回调
        FoliaScheduler.runOnPlayer(player, () -> {
            request.callback.accept(result);

            // 恢复GUI
            if (request.guiToRestore != null) {
                GUIManager guiManager = GUIManager.getInstance();
                if (guiManager != null) {
                    guiManager.open(player, request.guiToRestore);
                } else {
                    request.guiToRestore.open(player);
                }
            }
        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        pendingRequests.remove(playerId);
        originalBlocks.remove(playerId);
    }

    // ==================== 内部类 ====================

    private static class SignInputRequest {
        final Player player;
        final Location signLocation;
        final GUI guiToRestore;
        final Consumer<SignInputResult> callback;

        SignInputRequest(Player player, Location signLocation, GUI guiToRestore,
                         Consumer<SignInputResult> callback) {
            this.player = player;
            this.signLocation = signLocation;
            this.guiToRestore = guiToRestore;
            this.callback = callback;
        }
    }

    private static class BlockState {
        final Location location;
        final Block block;
        final Material material;
        final BlockData blockData;

        BlockState(Material material, BlockData blockData) {
            this.location = null;
            this.block = null;
            this.material = material;
            this.blockData = blockData;
        }

        BlockState(Block block, BlockData blockData) {
            this.location = block.getLocation();
            this.block = block;
            this.material = block.getType();
            this.blockData = blockData;
        }
    }

    /**
     * 告示牌输入结果
     */
    public static class SignInputResult {
        private final String text;
        private final boolean cancelled;
        private final Player player;

        public SignInputResult(@Nullable String text, boolean cancelled, @NotNull Player player) {
            this.text = text;
            this.cancelled = cancelled;
            this.player = player;
        }

        /**
         * 获取输入的文本
         * @return 文本内容
         */
        @Nullable
        public String getText() {
            return text;
        }

        /**
         * 检查是否被取消
         * @return 是否被取消
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * 检查是否成功获取输入
         * @return 是否成功
         */
        public boolean isSuccess() {
            return text != null && !text.isEmpty() && !cancelled;
        }

        /**
         * 获取玩家
         * @return 玩家实例
         */
        @NotNull
        public Player getPlayer() {
            return player;
        }
    }
}
