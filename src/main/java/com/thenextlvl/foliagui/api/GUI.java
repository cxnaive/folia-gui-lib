package com.thenextlvl.foliagui.api;

import com.thenextlvl.foliagui.api.content.ContentManager;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.CloseEvent;
import com.thenextlvl.foliagui.api.event.OpenEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * GUI接口 - 所有GUI的顶级接口
 * 定义了GUI的基本生命周期和操作方法
 *
 * @author TheNextLvl
 */
public interface GUI {

    /**
     * 获取GUI唯一标识
     * @return GUI ID
     */
    @NotNull
    String getId();

    /**
     * 打开GUI给指定玩家
     * @param player 玩家
     */
    void open(@NotNull Player player);

    /**
     * 关闭指定玩家的GUI
     * @param player 玩家
     */
    void close(@NotNull Player player);

    /**
     * 刷新整个GUI显示
     * <p>
     * 直接更新Inventory内容，不重新打开界面。
     * 这是无音效刷新的关键方法。
     */
    void refresh();

    /**
     * 更新指定槽位
     * @param slot 槽位
     */
    void updateSlot(int slot);

    /**
     * 刷新多个槽位（无音效）
     * <p>
     * 批量更新指定槽位，延迟合并执行以提高性能。
     * 直接操作Inventory，不重新打开界面。
     *
     * @param slots 槽位数组
     */
    default void refreshSlots(int... slots) {
        for (int slot : slots) {
            updateSlot(slot);
        }
    }

    /**
     * 释放GUI资源
     */
    void dispose();

    /**
     * 检查GUI是否已打开
     * @return 是否打开
     */
    boolean isOpen();

    /**
     * 获取当前查看此GUI的玩家
     * @return 玩家，如果没有则返回null
     */
    @Nullable
    Player getViewer();

    /**
     * 获取GUI绑定的位置（用于Region调度）
     * @return 位置，如果没有则返回null
     */
    @Nullable
    Location getLocation();

    /**
     * 获取Bukkit Inventory实例
     * @return Inventory
     */
    @Nullable
    Inventory getInventory();

    /**
     * 获取内容管理器
     * <p>
     * 用于管理动态内容并提供槽位索引查找，解决闭包变量问题。
     *
     * @return 内容管理器
     */
    @NotNull
    ContentManager getContentManager();

    /**
     * 动态获取槽位关联的数据
     * <p>
     * 解决点击处理器中的闭包问题：在创建处理器时变量被捕获，
     * 导致后续刷新后无法获取当前槽位对应的最新数据。
     * 使用此方法可以根据槽位动态查找最新数据。
     *
     * @param slot 槽位
     * @return 动态数据对象
     */
    @Nullable
    Object getSlotData(int slot);

    /**
     * 动态获取槽位关联的数据（带类型转换）
     *
     * @param slot 槽位
     * @param type 数据类型
     * @return 动态数据
     */
    @Nullable
    <T> T getSlotData(int slot, @NotNull Class<T> type);
    
    // ==================== 事件监听 ====================
    
    /**
     * 设置点击事件处理器
     * @param handler 处理器
     * @return 此GUI实例（链式调用）
     */
    GUI onClick(@NotNull Consumer<ClickEvent> handler);
    
    /**
     * 设置打开事件处理器
     * @param handler 处理器
     * @return 此GUI实例（链式调用）
     */
    GUI onOpen(@NotNull Consumer<OpenEvent> handler);
    
    /**
     * 设置关闭事件处理器
     * @param handler 处理器
     * @return 此GUI实例（链式调用）
     */
    GUI onClose(@NotNull Consumer<CloseEvent> handler);
    
    // ==================== 生命周期回调 ====================
    
    /**
     * 当GUI被打开时调用
     * @param player 玩家
     */
    default void onOpen(@NotNull Player player) {}
    
    /**
     * 当GUI被关闭时调用
     * @param player 玩家
     */
    default void onClose(@NotNull Player player) {}
    
    /**
     * 当槽位被点击时调用
     * @param event 点击事件
     */
    default void onClick(@NotNull ClickEvent event) {}
}
