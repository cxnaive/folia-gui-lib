package com.thenextlvl.foliagui.api.component;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;

/**
 * 动画图标组件接口
 * <p>
 * 单图标组件，按帧播放动画
 * 支持多种播放模式和帧控制
 *
 * @author TheNextLvl
 */
public interface AnimatedIcon extends Component {

    /**
     * 获取所有动画帧
     * @return 帧列表
     */
    @NotNull List<ItemStack> getFrames();

    /**
     * 设置动画帧
     * @param frames 帧列表
     * @return 此组件
     */
    @NotNull AnimatedIcon setFrames(@NotNull List<ItemStack> frames);

    /**
     * 添加帧
     * @param frame 帧
     * @return 此组件
     */
    @NotNull AnimatedIcon addFrame(@NotNull ItemStack frame);

    /**
     * 清除所有帧
     * @return 此组件
     */
    @NotNull AnimatedIcon clearFrames();

    /**
     * 获取当前帧索引
     * @return 帧索引
     */
    int getCurrentFrameIndex();

    /**
     * 设置当前帧
     * @param index 帧索引
     * @return 此组件
     */
    @NotNull AnimatedIcon setCurrentFrame(int index);

    /**
     * 获取当前帧
     * @return 当前帧物品
     */
    @NotNull ItemStack getCurrentFrame();

    /**
     * 下一帧
     * @return 此组件
     */
    @NotNull AnimatedIcon nextFrame();

    /**
     * 上一帧
     * @return 此组件
     */
    @NotNull AnimatedIcon previousFrame();

    /**
     * 获取帧间隔（ticks）
     * @return 间隔
     */
    int getInterval();

    /**
     * 设置帧间隔
     * @param ticks tick 数
     * @return 此组件
     */
    @NotNull AnimatedIcon interval(int ticks);

    /**
     * 获取播放模式
     * @return 播放模式
     */
    @NotNull PlayMode getPlayMode();

    /**
     * 设置播放模式
     * @param mode 播放模式
     * @return 此组件
     */
    @NotNull AnimatedIcon playMode(@NotNull PlayMode mode);

    /**
     * 是否正在播放
     * @return 是否播放中
     */
    boolean isPlaying();

    /**
     * 开始播放
     * @return 此组件
     */
    @NotNull AnimatedIcon play();

    /**
     * 停止播放
     * @return 此组件
     */
    @NotNull AnimatedIcon stop();

    /**
     * 暂停播放
     * @return 此组件
     */
    @NotNull AnimatedIcon pause();

    /**
     * 重置到第一帧
     * @return 此组件
     */
    @NotNull AnimatedIcon reset();

    /**
     * 设置帧变化监听器
     * @param listener 监听器
     * @return 此组件
     */
    @NotNull AnimatedIcon onFrameChange(@NotNull Consumer<FrameChangeEvent> listener);

    /**
     * 播放模式
     */
    enum PlayMode {
        /**
         * 正向播放一次
         */
        ONCE,

        /**
         * 循环播放
         */
        LOOP,

        /**
         * 来回播放（正向->反向->正向...）
         */
        PING_PONG,

        /**
         * 反向播放
         */
        REVERSE
    }

    /**
     * 帧变化事件
     */
    class FrameChangeEvent {
        private final AnimatedIcon animatedIcon;
        private final int oldFrame;
        private final int newFrame;

        public FrameChangeEvent(@NotNull AnimatedIcon animatedIcon, int oldFrame, int newFrame) {
            this.animatedIcon = animatedIcon;
            this.oldFrame = oldFrame;
            this.newFrame = newFrame;
        }

        public @NotNull AnimatedIcon getAnimatedIcon() {
            return animatedIcon;
        }

        public int getOldFrame() {
            return oldFrame;
        }

        public int getNewFrame() {
            return newFrame;
        }
    }
}