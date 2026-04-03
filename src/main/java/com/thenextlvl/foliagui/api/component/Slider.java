package com.thenextlvl.foliagui.api.component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 滑块组件接口
 * <p>
 * 单图标组件，在 Lore 中显示滑块轨道
 * 支持点击交互调整数值
 *
 * @author TheNextLvl
 */
public interface Slider extends Component {

    /**
     * 获取最小值
     * @return 最小值
     */
    double getMin();

    /**
     * 获取最大值
     * @return 最大值
     */
    double getMax();

    /**
     * 设置数值范围
     * @param min 最小值
     * @param max 最大值
     * @return 此组件
     */
    Slider range(double min, double max);

    /**
     * 获取当前值
     * @return 当前值
     */
    double getValue();

    /**
     * 设置当前值
     * @param value 数值
     * @return 此组件
     */
    Slider value(double value);

    /**
     * 获取步长
     * @return 步长
     */
    double getStep();

    /**
     * 设置步长
     * @param step 步长
     * @return 此组件
     */
    Slider step(double step);

    /**
     * 获取滑块轨道长度（字符数）
     * @return 长度
     */
    int getBarLength();

    /**
     * 设置滑块轨道长度
     * @param length 长度（字符数）
     * @return 此组件
     */
    Slider barLength(int length);

    /**
     * 获取样式
     * @return 样式
     */
    @NotNull Style getStyle();

    /**
     * 设置样式
     * @param style 样式
     * @return 此组件
     */
    Slider style(@NotNull Style style);

    /**
     * 获取轨道字符
     * @return 轨道字符
     */
    @NotNull String getTrackChar();

    /**
     * 设置轨道字符
     * @param charStr 字符
     * @return 此组件
     */
    Slider trackChar(@NotNull String charStr);

    /**
     * 获取滑块字符
     * @return 滑块字符
     */
    @NotNull String getThumbChar();

    /**
     * 设置滑块字符
     * @param charStr 字符
     * @return 此组件
     */
    Slider thumbChar(@NotNull String charStr);

    /**
     * 获取轨道颜色
     * @return 颜色代码
     */
    @NotNull String getTrackColor();

    /**
     * 设置轨道颜色
     * @param color 颜色代码（如 §7）
     * @return 此组件
     */
    Slider trackColor(@NotNull String color);

    /**
     * 获取滑块颜色
     * @return 颜色代码
     */
    @NotNull String getThumbColor();

    /**
     * 设置滑块颜色
     * @param color 颜色代码
     * @return 此组件
     */
    Slider thumbColor(@NotNull String color);

    /**
     * 设置基础物品
     * @param item 物品
     * @return 此组件
     */
    Slider baseItem(@NotNull ItemStack item);

    /**
     * 设置基础物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    Slider baseItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 设置显示名称
     * @param name 名称
     * @return 此组件
     */
    Slider displayName(@NotNull String name);

    /**
     * 设置是否显示数值
     * @param show 是否显示
     * @return 此组件
     */
    Slider showValue(boolean show);

    /**
     * 是否显示数值
     * @return 是否显示
     */
    boolean isShowValue();

    /**
     * 设置数值格式
     * @param format 格式（如 "%.1f"）
     * @return 此组件
     */
    Slider valueFormat(@NotNull String format);

    /**
     * 设置数值变化监听器
     * @param listener 监听器
     * @return 此组件
     */
    Slider onValueChange(@NotNull Consumer<ValueChangeEvent> listener);

    /**
     * 滑块样式
     */
    enum Style {
        /**
         * 方块样式 - 使用 █ 和 ▓
         */
        BLOCK("█", "▓"),

        /**
         * 箭头样式 - 使用 ► 和 =
         */
        ARROW("►", "="),

        /**
         * 圆点样式 - 使用 ● 和 ○
         */
        DOT("●", "○"),

        /**
         * 星号样式 - 使用 ★ 和 ☆
         */
        STAR("★", "☆"),

        /**
         * 文本样式 - 使用文字显示
         */
        TEXT("|", "-"),

        /**
         * 自定义样式
         */
        CUSTOM("", "");

        private final String defaultThumb;
        private final String defaultTrack;

        Style(String defaultThumb, String defaultTrack) {
            this.defaultThumb = defaultThumb;
            this.defaultTrack = defaultTrack;
        }

        public String getDefaultThumb() {
            return defaultThumb;
        }

        public String getDefaultTrack() {
            return defaultTrack;
        }
    }

    /**
     * 数值变化事件
     */
    class ValueChangeEvent {
        private final Slider slider;
        private final double oldValue;
        private final double newValue;
        private final Player player;
        private boolean cancelled = false;

        public ValueChangeEvent(@NotNull Slider slider, double oldValue, double newValue, @Nullable Player player) {
            this.slider = slider;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.player = player;
        }

        public @NotNull Slider getSlider() {
            return slider;
        }

        public double getOldValue() {
            return oldValue;
        }

        public double getNewValue() {
            return newValue;
        }

        public @Nullable Player getPlayer() {
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