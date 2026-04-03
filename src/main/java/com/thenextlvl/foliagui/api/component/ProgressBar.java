package com.thenextlvl.foliagui.api.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * 进度条组件接口
 * <p>
 * 单图标组件，在 Lore 中显示进度条
 * 支持多种样式：文本条、符号条、Unicode条等
 *
 * @author TheNextLvl
 */
public interface ProgressBar extends Component {

    /**
     * 获取当前进度 (0.0 - 1.0)
     * @return 进度值
     */
    double getProgress();

    /**
     * 设置进度
     * @param progress 进度值 (0.0 - 1.0)
     */
    void setProgress(double progress);

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
    ProgressBar range(double min, double max);

    /**
     * 获取当前数值
     * @return 当前值
     */
    double getValue();

    /**
     * 设置当前数值
     * @param value 数值
     * @return 此组件
     */
    ProgressBar value(double value);

    /**
     * 获取进度条长度（字符数）
     * @return 长度
     */
    int getBarLength();

    /**
     * 设置进度条长度
     * @param length 长度（字符数）
     * @return 此组件
     */
    ProgressBar barLength(int length);

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
    ProgressBar style(@NotNull Style style);

    /**
     * 获取填充字符
     * @return 填充字符
     */
    @NotNull String getFilledChar();

    /**
     * 设置填充字符
     * @param charStr 字符
     * @return 此组件
     */
    ProgressBar filledChar(@NotNull String charStr);

    /**
     * 获取空白字符
     * @return 空白字符
     */
    @NotNull String getEmptyChar();

    /**
     * 设置空白字符
     * @param charStr 字符
     * @return 此组件
     */
    ProgressBar emptyChar(@NotNull String charStr);

    /**
     * 获取填充颜色
     * @return 颜色代码
     */
    @NotNull String getFilledColor();

    /**
     * 设置填充颜色
     * @param color 颜色代码（如 §a, §c）
     * @return 此组件
     */
    ProgressBar filledColor(@NotNull String color);

    /**
     * 获取空白颜色
     * @return 颜色代码
     */
    @NotNull String getEmptyColor();

    /**
     * 设置空白颜色
     * @param color 颜色代码
     * @return 此组件
     */
    ProgressBar emptyColor(@NotNull String color);

    /**
     * 设置基础物品
     * @param item 物品
     * @return 此组件
     */
    ProgressBar baseItem(@NotNull ItemStack item);

    /**
     * 设置基础物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    ProgressBar baseItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 设置显示名称
     * @param name 名称
     * @return 此组件
     */
    ProgressBar displayName(@NotNull String name);

    /**
     * 设置是否显示百分比
     * @param show 是否显示
     * @return 此组件
     */
    ProgressBar showPercentage(boolean show);

    /**
     * 是否显示百分比
     * @return 是否显示
     */
    boolean isShowPercentage();

    /**
     * 设置百分比格式
     * @param format 格式（如 "%.1f%%"）
     * @return 此组件
     */
    ProgressBar percentageFormat(@NotNull String format);

    /**
     * 进度条样式
     */
    enum Style {
        /**
         * 方块样式 - 使用 █ 字符
         */
        BLOCK("█", "░"),

        /**
         * 箭头样式 - 使用 ► 和 ◄
         */
        ARROW("►", "◇"),

        /**
         * 星号样式 - 使用 ★ 和 ☆
         */
        STAR("★", "☆"),

        /**
         * 圆点样式 - 使用 ● 和 ○
         */
        DOT("●", "○"),

        /**
         * 心形样式 - 使用 ❤ 和 ♡
         */
        HEART("❤", "♡"),

        /**
         * 文本样式 - 使用文字显示
         */
        TEXT("[", "]"),

        /**
         * 自定义样式
         */
        CUSTOM("", "");

        private final String defaultFilled;
        private final String defaultEmpty;

        Style(String defaultFilled, String defaultEmpty) {
            this.defaultFilled = defaultFilled;
            this.defaultEmpty = defaultEmpty;
        }

        public String getDefaultFilled() {
            return defaultFilled;
        }

        public String getDefaultEmpty() {
            return defaultEmpty;
        }
    }
}
