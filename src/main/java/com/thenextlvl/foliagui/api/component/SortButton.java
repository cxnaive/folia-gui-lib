package com.thenextlvl.foliagui.api.component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;

/**
 * 排序按钮组件接口
 * 
 * @author TheNextLvl
 */
public interface SortButton extends Component {
    
    /**
     * 排序顺序枚举
     */
    enum SortOrder {
        ASCENDING("升序"),
        DESCENDING("降序");
        
        private final String displayName;
        
        SortOrder(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public SortOrder next() {
            return this == ASCENDING ? DESCENDING : ASCENDING;
        }
    }
    
    /**
     * 获取当前排序顺序
     * @return 排序顺序
     */
    @NotNull SortOrder getCurrentOrder();
    
    /**
     * 设置排序顺序
     * @param order 排序顺序
     * @return 此排序按钮
     */
    SortButton order(@NotNull SortOrder order);
    
    /**
     * 切换排序顺序
     * @return 此排序按钮
     */
    SortButton toggleOrder();
    
    /**
     * 获取升序状态物品
     * @return 物品
     */
    @NotNull ItemStack getAscendingItem();
    
    /**
     * 设置升序状态物品
     * @param item 物品
     * @return 此排序按钮
     */
    SortButton ascendingItem(@NotNull ItemStack item);
    
    /**
     * 获取降序状态物品
     * @return 物品
     */
    @NotNull ItemStack getDescendingItem();
    
    /**
     * 设置降序状态物品
     * @param item 物品
     * @return 此排序按钮
     */
    SortButton descendingItem(@NotNull ItemStack item);
    
    /**
     * 设置排序变化监听器
     * @param listener 监听器
     * @return 此排序按钮
     */
    SortButton onSortChange(@NotNull Consumer<SortChangeEvent> listener);
    
    /**
     * 对列表进行排序
     * @param <T> 元素类型
     * @param list 要排序的列表
     * @param comparator 比较器
     */
    <T> void sort(@NotNull List<T> list, @NotNull Comparator<T> comparator);
    
    /**
     * 排序变化事件
     */
    class SortChangeEvent {
        private final SortButton sortButton;
        private final SortOrder oldOrder;
        private final SortOrder newOrder;
        private final Player player;
        private boolean cancelled = false;
        
        public SortChangeEvent(SortButton sortButton, SortOrder oldOrder, SortOrder newOrder, Player player) {
            this.sortButton = sortButton;
            this.oldOrder = oldOrder;
            this.newOrder = newOrder;
            this.player = player;
        }
        
        public SortButton getSortButton() {
            return sortButton;
        }
        
        public SortOrder getOldOrder() {
            return oldOrder;
        }
        
        public SortOrder getNewOrder() {
            return newOrder;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
