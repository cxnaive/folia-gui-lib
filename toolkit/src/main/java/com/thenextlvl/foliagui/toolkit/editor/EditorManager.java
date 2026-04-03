package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 编辑管理器
 * <p>
 * 管理玩家的编辑会话
 *
 * @author TheNextLvl
 */
public class EditorManager {

    private final FoliaGUIToolkit plugin;
    private final Map<UUID, EditorSession> sessions = new HashMap<>();

    public EditorManager(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
    }

    /**
     * 创建编辑会话
     * <p>
     * 开始编辑指定的 GUI
     *
     * @param player    玩家
     * @param targetGUI 目标 GUI
     * @return 编辑会话
     */
    @NotNull
    public EditorSession createSession(@NotNull Player player, @NotNull GUI targetGUI) {
        // 如果已有会话，先结束
        EditorSession existing = sessions.get(player.getUniqueId());
        if (existing != null) {
            endSession(player);
        }

        // 创建新会话
        EditorSession session = new EditorSession(plugin, player, targetGUI);
        sessions.put(player.getUniqueId(), session);

        return session;
    }

    /**
     * 获取玩家的编辑会话
     *
     * @param player 玩家
     * @return 编辑会话，如果没有则返回 null
     */
    @Nullable
    public EditorSession getSession(@NotNull Player player) {
        return sessions.get(player.getUniqueId());
    }

    /**
     * 结束编辑会话
     *
     * @param player 玩家
     */
    public void endSession(@NotNull Player player) {
        EditorSession session = sessions.remove(player.getUniqueId());
        if (session != null) {
            session.cleanup();
            player.sendMessage("§e编辑会话已结束");
        }
    }

    /**
     * 检查玩家是否正在编辑
     *
     * @param player 玩家
     * @return 是否正在编辑
     */
    public boolean isEditing(@NotNull Player player) {
        return sessions.containsKey(player.getUniqueId());
    }

    /**
     * 获取正在编辑指定 GUI 的玩家
     *
     * @param guiId GUI ID
     * @return 玩家，如果没有则返回 null
     */
    @Nullable
    public Player getPlayerEditing(@NotNull String guiId) {
        for (Map.Entry<UUID, EditorSession> entry : sessions.entrySet()) {
            if (entry.getValue().getTargetGUI().getId().equals(guiId)) {
                return plugin.getServer().getPlayer(entry.getKey());
            }
        }
        return null;
    }

    /**
     * 检查 GUI 是否正在被编辑
     *
     * @param guiId GUI ID
     * @return 是否正在被编辑
     */
    public boolean isBeingEdited(@NotNull String guiId) {
        for (EditorSession session : sessions.values()) {
            if (session.getTargetGUI().getId().equals(guiId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取正在编辑的玩家数量
     *
     * @return 数量
     */
    public int getEditingCount() {
        return sessions.size();
    }

    /**
     * 保存所有编辑会话的更改
     */
    public void saveAll() {
        for (EditorSession session : sessions.values()) {
            Player player = session.getPlayer();
            if (player.isOnline()) {
                session.applyChanges();
                player.sendMessage("§e已保存 GUI: " + session.getTargetGUI().getId());
            }
        }
    }

    /**
     * 清理离线玩家的编辑会话
     */
    public void cleanupOfflinePlayers() {
        for (UUID playerId : new HashMap<>(sessions).keySet()) {
            Player player = plugin.getServer().getPlayer(playerId);
            if (player == null || !player.isOnline()) {
                EditorSession session = sessions.remove(playerId);
                if (session != null) {
                    session.cleanup();
                    plugin.getLogger().info("已清理离线玩家的编辑会话: " + playerId);
                }
            }
        }
    }
}