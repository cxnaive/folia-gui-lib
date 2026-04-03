package com.thenextlvl.foliagui.api.component;

import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 组件接口 - GUI的基本组成单元
 * 按钮、图标、装饰等都实现此接口
 * 
 * @author TheNextLvl
 */
public interface Component {
    
    /**
     * 获取组件唯一标识
     * @return 组件ID
     */
    @NotNull
    String getId();
    
    /**
     * 获取显示物品
     * @return ItemStack
     */
    @NotNull
    ItemStack getDisplayItem();
    
    /**
     * 设置显示物品
     * @param item 物品
     */
    void setDisplayItem(@NotNull ItemStack item);
    
    /**
     * 检查组件是否可交互
     * @return 是否可交互
     */
    boolean isInteractable();
    
    /**
     * 设置组件是否可交互
     * @param interactable 是否可交互
     */
    void setInteractable(boolean interactable);
    
    /**
     * 当组件被交互时调用
     * @param event 交互事件
     */
    void onInteract(@NotNull InteractionEvent event);
    
    /**
     * 克隆组件
     * @return 克隆的组件
     */
    @NotNull
    Component clone();
    
    /**
     * 获取组件所在的槽位
     * @return 槽位，如果未放置则返回-1
     */
    int getSlot();
    
    /**
     * 设置组件所在的槽位
     * @param slot 槽位
     */
    void setSlot(int slot);
    
    // ==================== 便捷方法 ====================
    
    /**
     * 设置点击事件处理器
     * @param handler 处理器
     * @return 此组件实例
     */
    Component onClick(@NotNull Consumer<ClickEvent> handler);
    
    /**
     * 设置组件权限
     * 玩家没有权限时组件会显示为锁定状态或隐藏
     * @param permission 权限节点
     * @return 此组件实例
     */
    Component permission(@Nullable String permission);
    
    /**
     * 获取组件显示条件
     * @return 条件表达式
     */
    @Nullable
    Predicate<Player> getCondition();

    /**
     * 设置组件显示条件
     * @param condition 条件表达式，接收Player参数返回boolean
     * @return 此组件实例
     */
    Component condition(@Nullable Predicate<Player> condition);

    /**
     * 设置刷新间隔
     * @param ticks tick数
     * @return 此组件实例
     */
    Component refreshInterval(int ticks);

    /**
     * 获取刷新间隔
     * @return tick数，-1表示不自动刷新
     */
    int getRefreshInterval();

    /**
     * 检查组件是否可移动
     * 可移动的组件允许玩家从GUI中取出
     * @return 是否可移动
     */
    boolean isMovable();
    
    /**
     * 设置组件是否可移动
     * @param movable 是否可移动
     */
    void setMovable(boolean movable);
    
    /**
     * 便捷方法：设置组件为可移动
     * @return 此组件实例
     */
    default Component movable() {
        setMovable(true);
        return this;
    }
    
    /**
     * 便捷方法：设置组件为不可移动
     * @return 此组件实例
     */
    default Component immovable() {
        setMovable(false);
        return this;
    }
}
