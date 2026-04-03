package com.thenextlvl.foliagui.internal.animation;

import com.thenextlvl.foliagui.api.animation.Animation;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import com.thenextlvl.foliagui.manager.GUIManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 动画实现类
 *
 * @author TheNextLvl
 */
public class AnimationImpl implements Animation {

    private final String id;
    private String name;
    private long duration = 1000;
    private long delay = 0;
    private boolean looping = false;
    private int loopCount = -1;
    private EasingFunction easing = EasingFunction.LINEAR;

    private final List<Consumer<Animation>> startListeners = new ArrayList<>();
    private final List<Consumer<Animation>> completeListeners = new ArrayList<>();
    private final List<Consumer<Double>> updateListeners = new ArrayList<>();
    private final List<Consumer<Integer>> loopCompleteListeners = new ArrayList<>();

    private boolean running = false;
    private boolean paused = false;
    private boolean reversed = false;
    private double progress = 0.0;
    private int currentLoop = 0;
    private long startTime = 0;
    private long pausedTime = 0;
    private ScheduledTask task;

    public AnimationImpl(@NotNull String id) {
        this.id = id;
        this.name = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public void setName(@NotNull String name) {
        this.name = name;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public Animation duration(long duration) {
        this.duration = Math.max(0, duration);
        return this;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public Animation delay(long delay) {
        this.delay = Math.max(0, delay);
        return this;
    }

    @Override
    public boolean isLooping() {
        return looping;
    }

    @Override
    public Animation looping(boolean looping) {
        this.looping = looping;
        return this;
    }

    @Override
    public int getLoopCount() {
        return loopCount;
    }

    @Override
    public Animation loopCount(int count) {
        this.loopCount = count;
        return this;
    }

    @Override
    public @NotNull EasingFunction getEasing() {
        return easing;
    }

    @Override
    public Animation easing(@NotNull EasingFunction easing) {
        this.easing = easing;
        return this;
    }

    @Override
    public void start() {
        if (running) {
            stop();
        }

        running = true;
        paused = false;
        progress = reversed ? 1.0 : 0.0;
        currentLoop = 0;
        startTime = System.currentTimeMillis() + delay;

        // 触发开始事件
        for (Consumer<Animation> listener : startListeners) {
            listener.accept(this);
        }

        // 启动动画任务
        startTask();
    }

    private void startTask() {
        task = FoliaScheduler.runTimerGlobal(delay, 50, scheduledTask -> {
            if (!running || paused) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            long elapsed = currentTime - startTime;

            if (elapsed < 0) {
                return; // 还在延迟阶段
            }

            // 计算进度
            double rawProgress = Math.min(1.0, (double) elapsed / duration);
            if (reversed) {
                rawProgress = 1.0 - rawProgress;
            }

            // 应用缓动函数
            progress = easing.ease(rawProgress);

            // 触发更新事件
            for (Consumer<Double> listener : updateListeners) {
                listener.accept(progress);
            }

            // 检查是否完成
            if (rawProgress >= 1.0 || (reversed && rawProgress <= 0.0)) {
                onComplete();
            }
        });
    }

    private void onComplete() {
        currentLoop++;

        // 触发循环完成事件
        for (Consumer<Integer> listener : loopCompleteListeners) {
            listener.accept(currentLoop);
        }

        // 检查是否需要继续循环
        if (looping && (loopCount < 0 || currentLoop < loopCount)) {
            startTime = System.currentTimeMillis();
            progress = reversed ? 1.0 : 0.0;
        } else {
            // 动画真正结束
            running = false;
            FoliaScheduler.cancelSafely(task);
            task = null;

            for (Consumer<Animation> listener : completeListeners) {
                listener.accept(this);
            }
        }
    }

    @Override
    public void stop() {
        running = false;
        paused = false;
        FoliaScheduler.cancelSafely(task);
        task = null;
    }

    @Override
    public void pause() {
        if (running && !paused) {
            paused = true;
            pausedTime = System.currentTimeMillis();
            FoliaScheduler.cancelSafely(task);
            task = null;
        }
    }

    @Override
    public void resume() {
        if (running && paused) {
            paused = false;
            long pauseDuration = System.currentTimeMillis() - pausedTime;
            startTime += pauseDuration;
            startTask();
        }
    }

    @Override
    public void reverse() {
        reversed = !reversed;
        if (running) {
            // 重新计算开始时间以保持当前进度
            double currentRawProgress = reversed ? 1.0 - progress : progress;
            long elapsed = (long) (currentRawProgress * duration);
            startTime = System.currentTimeMillis() - elapsed;
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    @Override
    public void setProgress(double progress) {
        this.progress = Math.max(0.0, Math.min(1.0, progress));
        if (running) {
            long elapsed = (long) (this.progress * duration);
            startTime = System.currentTimeMillis() - elapsed;
        }
    }

    @Override
    public Animation onComplete(@NotNull Consumer<Animation> listener) {
        completeListeners.add(listener);
        return this;
    }

    @Override
    public Animation onStart(@NotNull Consumer<Animation> listener) {
        startListeners.add(listener);
        return this;
    }

    @Override
    public Animation onUpdate(@NotNull Consumer<Double> listener) {
        updateListeners.add(listener);
        return this;
    }

    @Override
    public Animation onLoopComplete(@NotNull Consumer<Integer> listener) {
        loopCompleteListeners.add(listener);
        return this;
    }

    @Override
    public @NotNull Animation clone() {
        AnimationImpl clone = new AnimationImpl(id + "_clone");
        clone.name = name;
        clone.duration = duration;
        clone.delay = delay;
        clone.looping = looping;
        clone.loopCount = loopCount;
        clone.easing = easing;
        return clone;
    }
}
