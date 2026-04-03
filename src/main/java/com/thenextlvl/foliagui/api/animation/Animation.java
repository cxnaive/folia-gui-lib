package com.thenextlvl.foliagui.api.animation;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * 动画接口
 * <p>
 * 定义GUI动画的基本行为，包括开始、停止、暂停和恢复。
 *
 * @author TheNextLvl
 */
public interface Animation {

    /**
     * 获取动画ID
     * @return 唯一标识符
     */
    @NotNull String getId();

    /**
     * 获取动画名称
     * @return 动画名称
     */
    @NotNull String getName();

    /**
     * 获取动画持续时间（毫秒）
     * @return 持续时间
     */
    long getDuration();

    /**
     * 设置动画持续时间
     * @param duration 持续时间（毫秒）
     * @return 此动画
     */
    Animation duration(long duration);

    /**
     * 获取动画延迟时间（毫秒）
     * @return 延迟时间
     */
    long getDelay();

    /**
     * 设置动画延迟时间
     * @param delay 延迟时间（毫秒）
     * @return 此动画
     */
    Animation delay(long delay);

    /**
     * 是否循环播放
     * @return 是否循环
     */
    boolean isLooping();

    /**
     * 设置是否循环播放
     * @param looping 是否循环
     * @return 此动画
     */
    Animation looping(boolean looping);

    /**
     * 获取循环次数，-1表示无限循环
     * @return 循环次数
     */
    int getLoopCount();

    /**
     * 设置循环次数
     * @param count 循环次数，-1表示无限
     * @return 此动画
     */
    Animation loopCount(int count);

    /**
     * 获取缓动函数
     * @return 缓动函数
     */
    @NotNull EasingFunction getEasing();

    /**
     * 设置缓动函数
     * @param easing 缓动函数
     * @return 此动画
     */
    Animation easing(@NotNull EasingFunction easing);

    /**
     * 开始动画
     */
    void start();

    /**
     * 停止动画
     */
    void stop();

    /**
     * 暂停动画
     */
    void pause();

    /**
     * 恢复动画
     */
    void resume();

    /**
     * 反转动画方向
     */
    void reverse();

    /**
     * 检查动画是否正在运行
     * @return 是否运行中
     */
    boolean isRunning();

    /**
     * 检查动画是否已暂停
     * @return 是否暂停
     */
    boolean isPaused();

    /**
     * 获取当前进度（0.0 - 1.0）
     * @return 当前进度
     */
    double getProgress();

    /**
     * 设置当前进度
     * @param progress 进度值（0.0 - 1.0）
     */
    void setProgress(double progress);

    /**
     * 设置动画完成监听器
     * @param listener 监听器
     * @return 此动画
     */
    Animation onComplete(@NotNull Consumer<Animation> listener);

    /**
     * 设置动画开始监听器
     * @param listener 监听器
     * @return 此动画
     */
    Animation onStart(@NotNull Consumer<Animation> listener);

    /**
     * 设置动画更新监听器
     * @param listener 监听器，参数为当前进度（0.0 - 1.0）
     * @return 此动画
     */
    Animation onUpdate(@NotNull Consumer<Double> listener);

    /**
     * 设置动画循环完成监听器
     * @param listener 监听器，参数为当前循环次数
     * @return 此动画
     */
    Animation onLoopComplete(@NotNull Consumer<Integer> listener);

    /**
     * 克隆动画
     * @return 动画副本
     */
    @NotNull Animation clone();

    /**
     * 缓动函数接口
     */
    @FunctionalInterface
    interface EasingFunction {
        /**
         * 应用缓动
         * @param t 输入值（0.0 - 1.0）
         * @return 缓动后的值（0.0 - 1.0）
         */
        double ease(double t);

        // 预定义缓动函数
        EasingFunction LINEAR = t -> t;
        EasingFunction EASE_IN_QUAD = t -> t * t;
        EasingFunction EASE_OUT_QUAD = t -> 1 - (1 - t) * (1 - t);
        EasingFunction EASE_IN_OUT_QUAD = t -> t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
        EasingFunction EASE_IN_CUBIC = t -> t * t * t;
        EasingFunction EASE_OUT_CUBIC = t -> 1 - Math.pow(1 - t, 3);
        EasingFunction EASE_IN_OUT_CUBIC = t -> t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;
        EasingFunction EASE_IN_SINE = t -> 1 - Math.cos((t * Math.PI) / 2);
        EasingFunction EASE_OUT_SINE = t -> Math.sin((t * Math.PI) / 2);
        EasingFunction EASE_IN_OUT_SINE = t -> -(Math.cos(Math.PI * t) - 1) / 2;
        EasingFunction EASE_IN_BACK = t -> {
            double c1 = 1.70158;
            double c3 = c1 + 1;
            return c3 * t * t * t - c1 * t * t;
        };
        EasingFunction EASE_OUT_BACK = t -> {
            double c1 = 1.70158;
            double c3 = c1 + 1;
            return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
        };
        EasingFunction EASE_IN_OUT_BACK = t -> {
            double c1 = 1.70158;
            double c2 = c1 * 1.525;
            return t < 0.5
                    ? (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                    : (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
        };
        EasingFunction EASE_IN_ELASTIC = t -> {
            double c4 = (2 * Math.PI) / 3;
            return t == 0 ? 0 : t == 1 ? 1 : -Math.pow(2, 10 * t - 10) * Math.sin((t * 10 - 10.75) * c4);
        };
        EasingFunction EASE_OUT_ELASTIC = t -> {
            double c4 = (2 * Math.PI) / 3;
            return t == 0 ? 0 : t == 1 ? 1 : Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
        };
        EasingFunction EASE_OUT_BOUNCE = t -> {
            double n1 = 7.5625;
            double d1 = 2.75;
            if (t < 1 / d1) {
                return n1 * t * t;
            } else if (t < 2 / d1) {
                return n1 * (t -= 1.5 / d1) * t + 0.75;
            } else if (t < 2.5 / d1) {
                return n1 * (t -= 2.25 / d1) * t + 0.9375;
            } else {
                return n1 * (t -= 2.625 / d1) * t + 0.984375;
            }
        };
        EasingFunction EASE_IN_BOUNCE = t -> 1 - EASE_OUT_BOUNCE.ease(1 - t);
    }
}
