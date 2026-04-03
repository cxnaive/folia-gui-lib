package com.thenextlvl.foliagui.manager;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.history.GUIHistory;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import com.thenextlvl.foliagui.internal.AbstractGUI;
import com.thenextlvl.foliagui.internal.history.GUIHistoryImpl;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GUI管理器
 * 统一管理所有GUI实例，处理注册、打开、关闭等操作
 * 
 * @author TheNextLvl
 */
public class GUIManager {
    
    private static GUIManager instance;
    private final Plugin plugin;
    private final Map<String, GUI> registeredGUIs = new ConcurrentHashMap<>();
    private final Map<UUID, GUI> playerOpenGUIs = new ConcurrentHashMap<>();
    private final Map<UUID, GUIHistoryImpl> playerHistories = new ConcurrentHashMap<>();
    
    private GUIManager(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 初始化GUIManager
     * @param plugin 插件实例
     * @return GUIManager实例
     */
    public static synchronized GUIManager init(@NotNull Plugin plugin) {
        if (instance == null) {
            instance = new GUIManager(plugin);
            // 初始化FoliaScheduler
            FoliaScheduler.init(plugin);
            // 初始化ChatInputManager
            ChatInputManager.init(plugin);
            // 初始化SignInputManager
            SignInputManager.init(plugin);
        }
        return instance;
    }
    
    /**
     * 获取GUIManager实例
     * @return 实例，如果未初始化则返回null
     */
    public static @Nullable GUIManager getInstance() {
        return instance;
    }

    /**
     * 获取插件实例
     * @return 插件实例
     */
    public @NotNull Plugin getPlugin() {
        return plugin;
    }
    
    // ==================== GUI注册 ====================
    
    /**
     * 注册GUI
     * @param gui GUI实例
     * @return 此管理器
     */
    public GUIManager register(@NotNull GUI gui) {
        registeredGUIs.put(gui.getId(), gui);
        return this;
    }
    
    /**
     * 注册多个GUI
     * @param guis GUI实例数组
     * @return 此管理器
     */
    public GUIManager register(@NotNull GUI... guis) {
        for (GUI gui : guis) {
            register(gui);
        }
        return this;
    }
    
    /**
     * 注销GUI
     * @param id GUI ID
     * @return 此管理器
     */
    public GUIManager unregister(@NotNull String id) {
        GUI gui = registeredGUIs.remove(id);
        if (gui != null) {
            gui.dispose();
        }
        return this;
    }
    
    /**
     * 获取已注册的GUI
     * @param id GUI ID
     * @return GUI实例，如果没有则返回null
     */
    public @Nullable GUI getGUI(@NotNull String id) {
        return registeredGUIs.get(id);
    }
    
    /**
     * 检查GUI是否已注册
     * @param id GUI ID
     * @return 是否已注册
     */
    public boolean isRegistered(@NotNull String id) {
        return registeredGUIs.containsKey(id);
    }
    
    /**
     * 获取所有已注册的GUI
     * @return GUI集合
     */
    public @NotNull Collection<GUI> getAllGUIs() {
        return Collections.unmodifiableCollection(registeredGUIs.values());
    }
    
    /**
     * 获取所有已注册的GUI ID
     * @return ID集合
     */
    public @NotNull Set<String> getAllGUIIds() {
        return Collections.unmodifiableSet(registeredGUIs.keySet());
    }
    
    // ==================== GUI打开/关闭 ====================
    
    /**
     * 打开GUI给玩家
     * @param player 玩家
     * @param guiId GUI ID
     * @return 是否成功打开
     */
    public boolean open(@NotNull Player player, @NotNull String guiId) {
        GUI gui = registeredGUIs.get(guiId);
        if (gui == null) {
            plugin.getLogger().warning("GUI not found: " + guiId);
            return false;
        }
        return open(player, gui);
    }
    
    /**
     * 打开GUI给玩家
     * @param player 玩家
     * @param gui GUI实例
     * @return 是否成功打开
     */
    public boolean open(@NotNull Player player, @NotNull GUI gui) {
        // 关闭玩家当前打开的GUI
        close(player);
        
        // 记录玩家打开的GUI
        playerOpenGUIs.put(player.getUniqueId(), gui);
        
        // 打开GUI
        gui.open(player);
        return true;
    }
    
    /**
     * 关闭玩家当前打开的GUI
     * @param player 玩家
     * @return 是否成功关闭
     */
    public boolean close(@NotNull Player player) {
        GUI gui = playerOpenGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.close(player);
            return true;
        }
        return false;
    }
    
    /**
     * 获取玩家当前打开的GUI
     * @param player 玩家
     * @return GUI实例，如果没有则返回null
     */
    public @Nullable GUI getOpenGUI(@NotNull Player player) {
        return playerOpenGUIs.get(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否有打开的GUI
     * @param player 玩家
     * @return 是否有打开的GUI
     */
    public boolean hasOpenGUI(@NotNull Player player) {
        return playerOpenGUIs.containsKey(player.getUniqueId());
    }
    
    // ==================== 批量操作 ====================
    
    /**
     * 关闭所有玩家打开的GUI
     */
    public void closeAll() {
        for (Map.Entry<UUID, GUI> entry : playerOpenGUIs.entrySet()) {
            Player player = plugin.getServer().getPlayer(entry.getKey());
            if (player != null && player.isOnline()) {
                entry.getValue().close(player);
            }
        }
        playerOpenGUIs.clear();
    }
    
    /**
     * 释放所有GUI资源
     */
    public void disposeAll() {
        closeAll();
        for (GUI gui : registeredGUIs.values()) {
            gui.dispose();
        }
        registeredGUIs.clear();
    }
    
    // ==================== 玩家状态管理 ====================
    
    /**
     * 当玩家退出时清理
     * @param player 玩家
     */
    public void onPlayerQuit(@NotNull Player player) {
        playerOpenGUIs.remove(player.getUniqueId());
    }
    
    /**
     * 获取当前打开GUI的玩家列表
     * @return 玩家UUID列表
     */
    public @NotNull Set<UUID> getPlayersWithOpenGUI() {
        return Collections.unmodifiableSet(playerOpenGUIs.keySet());
    }
    
    /**
     * 获取当前打开GUI的玩家数量
     * @return 数量
     */
    public int getOpenGUICount() {
        return playerOpenGUIs.size();
    }
    
    /**
     * 获取已注册的GUI数量
     * @return 数量
     */
    public int getRegisteredGUICount() {
        return registeredGUIs.size();
    }

    // ==================== 历史管理 ====================

    /**
     * 获取玩家的GUI历史
     * @param player 玩家
     * @return 历史实例
     */
    @NotNull
    public GUIHistory getHistory(@NotNull Player player) {
        return playerHistories.computeIfAbsent(player.getUniqueId(),
                id -> new GUIHistoryImpl(player));
    }

    /**
     * 打开GUI并记录到历史
     * <p>
     * 使用此方法打开的GUI会被记录到历史中，支持后退/前进导航
     *
     * @param player 玩家
     * @param gui GUI实例
     * @return 是否成功打开
     */
    public boolean openWithHistory(@NotNull Player player, @NotNull GUI gui) {
        GUIHistory history = getHistory(player);
        history.add(gui);
        return open(player, gui);
    }

    /**
     * 打开GUI并记录到历史
     *
     * @param player 玩家
     * @param guiId GUI ID
     * @return 是否成功打开
     */
    public boolean openWithHistory(@NotNull Player player, @NotNull String guiId) {
        GUI gui = registeredGUIs.get(guiId);
        if (gui == null) {
            plugin.getLogger().warning("GUI not found: " + guiId);
            return false;
        }
        return openWithHistory(player, gui);
    }

    /**
     * 打开GUI并记录到历史（自定义标题）
     *
     * @param player 玩家
     * @param gui GUI实例
     * @param title 历史记录中显示的标题
     * @return 是否成功打开
     */
    public boolean openWithHistory(@NotNull Player player, @NotNull GUI gui, @NotNull String title) {
        GUIHistory history = getHistory(player);
        history.add(gui, title, null);
        return open(player, gui);
    }

    /**
     * 后退到上一个GUI
     *
     * @param player 玩家
     * @return 是否成功后退
     */
    public boolean goBack(@NotNull Player player) {
        GUIHistory history = getHistory(player);
        return history.goBack();
    }

    /**
     * 前进到下一个GUI
     *
     * @param player 玩家
     * @return 是否成功前进
     */
    public boolean goForward(@NotNull Player player) {
        GUIHistory history = getHistory(player);
        return history.goForward();
    }

    /**
     * 清除玩家的GUI历史
     *
     * @param player 玩家
     */
    public void clearHistory(@NotNull Player player) {
        GUIHistoryImpl history = playerHistories.remove(player.getUniqueId());
        if (history != null) {
            history.clear();
        }
    }

    /**
     * 清除所有玩家的GUI历史
     */
    public void clearAllHistories() {
        for (GUIHistoryImpl history : playerHistories.values()) {
            history.clear();
        }
        playerHistories.clear();
    }
}
