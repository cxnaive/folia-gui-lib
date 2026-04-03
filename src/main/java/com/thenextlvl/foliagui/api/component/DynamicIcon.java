package com.thenextlvl.foliagui.api.component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * 动态图标组件接口
 * <p>
 * 根据数据提供器动态生成显示物品的组件
 * 支持定时刷新和条件显示
 * <pre>
 * DynamicIcon icon = new DynamicIconImpl("flower")
 *     .dataProvider(player -> getFlowerDisplay(player))
 *     .updateInterval(20)
 *     .condition(player -> hasFlower(player));
 * </pre>
 *
 * @author TheNextLvl
 */
public interface DynamicIcon extends Component {

    /**
     * 获取数据提供器
     *
     * @return 数据提供器
     */
    @Nullable
    Function<Player, ItemStack> getDataProvider();

    /**
     * 设置数据提供器
     * <p>
     * 数据提供器根据玩家状态返回要显示的物品
     *
     * @param provider 数据提供器
     * @return 此组件
     */
    @NotNull
    DynamicIcon dataProvider(@Nullable Function<Player, ItemStack> provider);

    /**
     * 获取更新间隔
     *
     * @return 更新间隔（tick）
     */
    int getUpdateInterval();

    /**
     * 设置更新间隔
     *
     * @param ticks 更新间隔（tick），0 或负数表示不自动更新
     * @return 此组件
     */
    @NotNull
    DynamicIcon updateInterval(int ticks);

    /**
     * 是否异步更新
     *
     * @return 是否异步
     */
    boolean isAsync();

    /**
     * 设置是否异步更新
     *
     * @param async 是否异步
     * @return 此组件
     */
    @NotNull
    DynamicIcon async(boolean async);

    /**
     * 获取条件不满足时的替代图标
     *
     * @return 替代图标提供器
     */
    @Nullable
    Function<Player, ItemStack> getElseProvider();

    /**
     * 设置条件不满足时的替代图标
     *
     * @param provider 替代图标提供器
     * @return 此组件
     */
    @NotNull
    DynamicIcon elseProvider(@Nullable Function<Player, ItemStack> provider);

    /**
     * 刷新显示
     * <p>
     * 重新调用数据提供器获取最新物品
     *
     * @param player 当前查看的玩家
     */
    void refresh(@NotNull Player player);

    /**
     * 强制设置当前显示物品
     * <p>
     * 跳过数据提供器，直接设置显示物品
     *
     * @param item 物品
     */
    void setCurrentItem(@Nullable ItemStack item);

    /**
     * 获取当前显示的物品（不刷新）
     *
     * @return 当前物品
     */
    @Nullable
    ItemStack getCurrentItem();
}