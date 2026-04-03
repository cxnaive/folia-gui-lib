package com.thenextlvl.foliagui.api.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * 分页组件接口
 * 用于在GUI中显示多页内容
 *
 * @author TheNextLvl
 */
public interface Pagination extends Component {

    /**
     * 获取当前页码（从1开始）
     * @return 当前页码
     */
    int getCurrentPage();

    /**
     * 获取总页数
     * @return 总页数
     */
    int getTotalPages();

    /**
     * 获取每页显示的物品数量
     * @return 每页物品数
     */
    int getItemsPerPage();

    /**
     * 获取所有分页内容
     * @return 分页内容列表
     */
    @NotNull List<ItemStack> getItems();

    /**
     * 设置分页内容
     * @param items 物品列表
     */
    void setItems(@NotNull List<ItemStack> items);

    /**
     * 添加物品
     * @param item 物品
     */
    void addItem(@NotNull ItemStack item);

    /**
     * 清空所有物品
     */
    void clearItems();

    /**
     * 跳转到指定页
     * @param page 页码（从1开始）
     */
    void goToPage(int page);

    /**
     * 下一页
     * @return 是否成功跳转
     */
    boolean nextPage();

    /**
     * 上一页
     * @return 是否成功跳转
     */
    boolean previousPage();

    /**
     * 是否有下一页
     * @return 是否有下一页
     */
    boolean hasNextPage();

    /**
     * 是否有上一页
     * @return 是否有上一页
     */
    boolean hasPreviousPage();

    /**
     * 获取当前页的物品
     * @return 当前页物品列表
     */
    @NotNull List<ItemStack> getCurrentPageItems();

    /**
     * 获取当前页物品所在的槽位
     * @return 槽位列表
     */
    @NotNull int[] getContentSlots();

    /**
     * 设置内容槽位
     * @param slots 槽位数组
     */
    void setContentSlots(@NotNull int[] slots);

    /**
     * 获取上一页按钮槽位
     * @return 槽位
     */
    int getPreviousPageSlot();

    /**
     * 设置上一页按钮槽位
     * @param slot 槽位
     */
    void setPreviousPageSlot(int slot);

    /**
     * 获取下一页按钮槽位
     * @return 槽位
     */
    int getNextPageSlot();

    /**
     * 设置下一页按钮槽位
     * @param slot 槽位
     */
    void setNextPageSlot(int slot);

    /**
     * 获取页码显示槽位
     * @return 槽位，-1表示不显示
     */
    int getPageIndicatorSlot();

    /**
     * 设置页码显示槽位
     * @param slot 槽位，-1表示不显示
     */
    void setPageIndicatorSlot(int slot);

    /**
     * 设置分页点击处理器
     * @param handler 处理器
     */
    void onPaginationClick(@NotNull Consumer<PaginationClickEvent> handler);

    /**
     * 设置页面切换回调
     * @param callback 回调
     */
    void onPageChange(@NotNull Runnable callback);

    /**
     * 分页点击事件
     */
    class PaginationClickEvent {
        private final org.bukkit.entity.Player player;
        private final ItemStack clickedItem;
        private final int index;
        private final int globalIndex;
        private boolean cancelled = false;

        public PaginationClickEvent(org.bukkit.entity.Player player, ItemStack clickedItem, int index, int globalIndex) {
            this.player = player;
            this.clickedItem = clickedItem;
            this.index = index;
            this.globalIndex = globalIndex;
        }

        public org.bukkit.entity.Player getPlayer() {
            return player;
        }

        public ItemStack getClickedItem() {
            return clickedItem;
        }

        public int getIndex() {
            return index;
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
