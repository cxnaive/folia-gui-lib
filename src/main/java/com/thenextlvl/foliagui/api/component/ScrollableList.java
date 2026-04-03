package com.thenextlvl.foliagui.api.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 可滚动列表组件接口
 * <p>
 * 在GUI中显示一个可滚动的物品列表，支持逐项或逐行滚动
 *
 * @author TheNextLvl
 */
public interface ScrollableList extends Component {

    /**
     * 获取列表中的所有物品
     * @return 物品列表
     */
    @NotNull List<ItemStack> getItems();

    /**
     * 设置列表物品
     * @param items 物品列表
     * @return 此组件
     */
    @NotNull ScrollableList setItems(@NotNull List<ItemStack> items);

    /**
     * 添加物品到列表
     * @param item 物品
     * @return 此组件
     */
    @NotNull ScrollableList addItem(@NotNull ItemStack item);

    /**
     * 清空列表
     * @return 此组件
     */
    @NotNull ScrollableList clearItems();

    /**
     * 获取当前滚动偏移
     * @return 偏移值
     */
    int getScrollOffset();

    /**
     * 设置滚动偏移
     * @param offset 偏移值
     * @return 此组件
     */
    @NotNull ScrollableList setScrollOffset(int offset);

    /**
     * 向上滚动
     * @param amount 滚动数量
     * @return 是否成功滚动
     */
    boolean scrollUp(int amount);

    /**
     * 向下滚动
     * @param amount 滚动数量
     * @return 是否成功滚动
     */
    boolean scrollDown(int amount);

    /**
     * 滚动到顶部
     * @return 是否成功滚动
     */
    boolean scrollToTop();

    /**
     * 滚动到底部
     * @return 是否成功滚动
     */
    boolean scrollToBottom();

    /**
     * 检查是否可以向上滚动
     * @return 是否可以向上滚动
     */
    boolean canScrollUp();

    /**
     * 检查是否可以向下滚动
     * @return 是否可以向下滚动
     */
    boolean canScrollDown();

    /**
     * 获取可见物品数量
     * @return 可见数量
     */
    int getVisibleCount();

    /**
     * 设置可见物品数量
     * @param count 可见数量
     * @return 此组件
     */
    @NotNull ScrollableList setVisibleCount(int count);

    /**
     * 获取当前可见的物品
     * @return 可见物品列表
     */
    @NotNull List<ItemStack> getVisibleItems();

    /**
     * 获取内容槽位
     * @return 槽位数组
     */
    @NotNull int[] getContentSlots();

    /**
     * 设置内容槽位
     * @param slots 槽位数组
     * @return 此组件
     */
    @NotNull ScrollableList setContentSlots(@NotNull int[] slots);

    /**
     * 获取向上滚动按钮槽位
     * @return 槽位，-1表示未设置
     */
    int getScrollUpSlot();

    /**
     * 设置向上滚动按钮槽位
     * @param slot 槽位
     * @return 此组件
     */
    @NotNull ScrollableList setScrollUpSlot(int slot);

    /**
     * 获取向下滚动按钮槽位
     * @return 槽位，-1表示未设置
     */
    int getScrollDownSlot();

    /**
     * 设置向下滚动按钮槽位
     * @param slot 槽位
     * @return 此组件
     */
    @NotNull ScrollableList setScrollDownSlot(int slot);

    /**
     * 设置滚动按钮物品
     * @param upItem 向上滚动按钮
     * @param downItem 向下滚动按钮
     * @return 此组件
     */
    @NotNull ScrollableList setScrollButtons(@Nullable ItemStack upItem, @Nullable ItemStack downItem);

    /**
     * 设置点击处理器
     * @param handler 处理器
     * @return 此组件
     */
    @NotNull ScrollableList onItemClick(@NotNull Consumer<ScrollableItemClickEvent> handler);

    /**
     * 设置滚动回调
     * @param callback 回调
     * @return 此组件
     */
    @NotNull ScrollableList onScroll(@NotNull Runnable callback);

    /**
     * 获取向上滚动按钮物品
     * @return 物品
     */
    @NotNull ItemStack getScrollUpItem();

    /**
     * 获取向下滚动按钮物品
     * @return 物品
     */
    @NotNull ItemStack getScrollDownItem();

    /**
     * 获取滚动指示器槽位
     * @return 槽位，-1表示未设置
     */
    int getScrollIndicatorSlot();

    /**
     * 设置滚动指示器槽位
     * @param slot 槽位
     * @return 此组件
     */
    @NotNull ScrollableList setScrollIndicatorSlot(int slot);

    /**
     * 获取滚动指示器物品
     * @return 物品
     */
    @NotNull ItemStack getScrollIndicatorItem();

    /**
     * 滚动项点击事件
     */
    class ScrollableItemClickEvent {
        private final org.bukkit.entity.Player player;
        private final ItemStack clickedItem;
        private final int visibleIndex;
        private final int globalIndex;
        private boolean cancelled = false;

        public ScrollableItemClickEvent(@NotNull org.bukkit.entity.Player player,
                                        @NotNull ItemStack clickedItem,
                                        int visibleIndex, int globalIndex) {
            this.player = player;
            this.clickedItem = clickedItem;
            this.visibleIndex = visibleIndex;
            this.globalIndex = globalIndex;
        }

        @NotNull
        public org.bukkit.entity.Player getPlayer() {
            return player;
        }

        @NotNull
        public ItemStack getClickedItem() {
            return clickedItem;
        }

        public int getVisibleIndex() {
            return visibleIndex;
        }

        public int getGlobalIndex() {
            return globalIndex;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
