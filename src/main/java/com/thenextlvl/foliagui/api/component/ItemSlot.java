package com.thenextlvl.foliagui.api.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 物品填充格组件
 * <p>
 * 一个可以接收玩家放入物品的槽位组件：
 * - 可以设置为空格或显示占位符
 * - 界面关闭时自动返还物品（可配置）
 * - 玩家掉线时自动返还物品
 *
 * @author TheNextLvl
 */
public interface ItemSlot extends Component {

    /**
     * 获取槽位中当前存放的物品
     * @return 物品，如果为空则返回 null
     */
    @Nullable
    ItemStack getStoredItem();

    /**
     * 设置槽位中的物品
     * @param item 物品
     */
    void setStoredItem(@Nullable ItemStack item);

    /**
     * 获取占位符物品
     * <p>
     * 当槽位为空时显示的物品，可以设置为 null 表示完全空白
     * @return 占位符物品
     */
    @Nullable
    ItemStack getPlaceholder();

    /**
     * 设置占位符物品
     * @param placeholder 占位符物品
     * @return 此组件
     */
    @NotNull
    ItemSlot placeholder(@Nullable ItemStack placeholder);

    /**
     * 是否自动返还物品
     * <p>
     * 当 GUI 关闭或玩家掉线时是否自动返还槽位中的物品给玩家
     * @return 是否自动返还
     */
    boolean isAutoReturn();

    /**
     * 设置是否自动返还物品
     * @param autoReturn 是否自动返还
     * @return 此组件
     */
    @NotNull
    ItemSlot autoReturn(boolean autoReturn);

    /**
     * 设置物品放入后的回调
     * @param callback 回调函数
     * @return 此组件
     */
    @NotNull
    ItemSlot onItemPlaced(@Nullable Consumer<ItemStack> callback);

    /**
     * 设置物品取出后的回调
     * @param callback 回调函数
     * @return 此组件
     */
    @NotNull
    ItemSlot onItemRemoved(@Nullable Consumer<ItemStack> callback);

    /**
     * 设置物品变化的统一回调
     * <p>
     * 无论是放入还是取出物品，都会触发此回调
     * 提供更简洁的方式来监听槽位状态变化
     * <pre>
     * itemSlot.onItemChange((previous, current) -> {
     *     if (current != null) {
     *         player.sendMessage("放入了: " + current.getType());
     *     } else {
     *         player.sendMessage("取出了: " + previous.getType());
     *     }
     * });
     * </pre>
     *
     * @param callback 回调函数，参数为 (previousItem, currentItem)
     * @return 此组件
     */
    @NotNull
    ItemSlot onItemChange(@Nullable ItemChangeCallback callback);

    /**
     * 物品变化回调接口
     */
    @FunctionalInterface
    interface ItemChangeCallback {
        /**
         * 物品变化时调用
         * @param previous 变化前的物品（可能为 null）
         * @param current  变化后的物品（可能为 null）
         */
        void onChange(@Nullable ItemStack previous, @Nullable ItemStack current);
    }

    /**
     * 设置物品数量限制
     * <p>
     * 限制玩家可以放入的最大物品数量
     * @param limit 数量限制（-1 表示无限制）
     * @return 此组件
     */
    @NotNull
    ItemSlot stackLimit(int limit);

    /**
     * 获取物品数量限制
     * @return 数量限制
     */
    int getStackLimit();

    /**
     * 设置物品类型过滤
     * <p>
     * 只有符合过滤条件的物品才能放入
     * @param filter 过滤函数
     * @return 此组件
     */
    @NotNull
    ItemSlot itemFilter(@Nullable java.util.function.Predicate<ItemStack> filter);

    /**
     * 检查物品是否可以放入
     * @param item 物品
     * @return 是否可以放入
     */
    boolean canAccept(@NotNull ItemStack item);

    /**
     * 清空槽位
     * @return 被清空的物品
     */
    @Nullable
    ItemStack clear();
}