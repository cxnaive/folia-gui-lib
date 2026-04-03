package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.AnimatedIcon;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 动画图标组件实现
 *
 * @author TheNextLvl
 */
public class AnimatedIconImpl implements AnimatedIcon {

    private final String id;
    private final List<ItemStack> frames = new ArrayList<>();
    private int currentFrame = 0;
    private int interval = 20; // 默认 1 秒（20 ticks）
    private PlayMode playMode = PlayMode.LOOP;
    private boolean playing = false;
    private boolean forward = true; // 用于 PING_PONG 模式
    private Object task; // ScheduledTask
    private Consumer<FrameChangeEvent> frameChangeListener;
    private Consumer<ClickEvent> clickHandler;

    // 组件基础属性
    private int slot = -1;
    private boolean interactable = false;
    private boolean movable = false;
    private Runnable updateCallback; // 用于通知 GUI 更新
    private Player boundPlayer; // 绑定的玩家，用于调度器

    public AnimatedIconImpl(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull List<ItemStack> getFrames() {
        return new ArrayList<>(frames);
    }

    @Override
    public @NotNull AnimatedIcon setFrames(@NotNull List<ItemStack> frames) {
        this.frames.clear();
        for (ItemStack frame : frames) {
            this.frames.add(frame.clone());
        }
        this.currentFrame = 0;
        return this;
    }

    @Override
    public @NotNull AnimatedIcon addFrame(@NotNull ItemStack frame) {
        this.frames.add(frame.clone());
        return this;
    }

    @Override
    public @NotNull AnimatedIcon clearFrames() {
        this.frames.clear();
        this.currentFrame = 0;
        return this;
    }

    @Override
    public int getCurrentFrameIndex() {
        return currentFrame;
    }

    @Override
    public @NotNull AnimatedIcon setCurrentFrame(int index) {
        if (frames.isEmpty()) {
            this.currentFrame = 0;
        } else {
            this.currentFrame = Math.max(0, Math.min(index, frames.size() - 1));
        }
        return this;
    }

    @Override
    public @NotNull ItemStack getCurrentFrame() {
        if (frames.isEmpty()) {
            return getDefaultItem();
        }
        return frames.get(currentFrame).clone();
    }

    @Override
    public @NotNull AnimatedIcon nextFrame() {
        if (frames.isEmpty()) return this;

        int oldFrame = currentFrame;

        switch (playMode) {
            case ONCE -> {
                if (currentFrame < frames.size() - 1) {
                    currentFrame++;
                }
            }
            case LOOP -> {
                currentFrame = (currentFrame + 1) % frames.size();
            }
            case PING_PONG -> {
                if (forward) {
                    if (currentFrame < frames.size() - 1) {
                        currentFrame++;
                    } else {
                        forward = false;
                        currentFrame--;
                    }
                } else {
                    if (currentFrame > 0) {
                        currentFrame--;
                    } else {
                        forward = true;
                        currentFrame++;
                    }
                }
            }
            case REVERSE -> {
                currentFrame = (currentFrame - 1 + frames.size()) % frames.size();
            }
        }

        if (oldFrame != currentFrame && frameChangeListener != null) {
            frameChangeListener.accept(new FrameChangeEvent(this, oldFrame, currentFrame));
        }

        return this;
    }

    @Override
    public @NotNull AnimatedIcon previousFrame() {
        if (frames.isEmpty()) return this;

        int oldFrame = currentFrame;
        currentFrame = (currentFrame - 1 + frames.size()) % frames.size();

        if (oldFrame != currentFrame && frameChangeListener != null) {
            frameChangeListener.accept(new FrameChangeEvent(this, oldFrame, currentFrame));
        }

        return this;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public @NotNull AnimatedIcon interval(int ticks) {
        this.interval = Math.max(1, ticks);
        return this;
    }

    @Override
    public @NotNull PlayMode getPlayMode() {
        return playMode;
    }

    @Override
    public @NotNull AnimatedIcon playMode(@NotNull PlayMode mode) {
        this.playMode = mode;
        this.forward = true;
        return this;
    }

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public @NotNull AnimatedIcon play() {
        if (playing || frames.isEmpty()) return this;

        // 如果没有绑定玩家，延迟播放（等待 GUI 打开时绑定）
        // 这样可以确保动画在正确的线程运行
        if (boundPlayer == null) {
            playing = true; // 标记为播放中，等待 startComponents() 启动
            return this;
        }

        playing = true;
        startAnimation();
        return this;
    }

    @Override
    public @NotNull AnimatedIcon stop() {
        playing = false;
        stopAnimation();
        currentFrame = 0;
        return this;
    }

    @Override
    public @NotNull AnimatedIcon pause() {
        playing = false;
        stopAnimation();
        return this;
    }

    @Override
    public @NotNull AnimatedIcon reset() {
        currentFrame = 0;
        forward = true;
        return this;
    }

    @Override
    public @NotNull AnimatedIcon onFrameChange(@NotNull Consumer<FrameChangeEvent> listener) {
        this.frameChangeListener = listener;
        return this;
    }

    /**
     * 设置更新回调（由 GUI 调用）
     */
    public void setUpdateCallback(@Nullable Runnable callback) {
        this.updateCallback = callback;
    }

    /**
     * 绑定玩家（用于正确的线程调度）
     * 由 AbstractGUI.startComponents() 调用
     */
    public void bindPlayer(@Nullable Player player) {
        this.boundPlayer = player;
    }

    /**
     * 获取当前任务对象（用于检查动画是否已启动）
     */
    @Nullable
    public Object getTask() {
        return task;
    }

    /**
     * 启动动画（内部方法，由 GUI 调用）
     */
    public void startAnimation() {
        Plugin plugin = getPlugin();
        if (plugin == null) return;

        // 使用玩家绑定的调度器，确保在正确的线程执行
        // delay=0 表示立即开始第一帧，period=interval 表示每间隔更新
        if (boundPlayer != null) {
            task = FoliaScheduler.runTimerOnPlayer(boundPlayer, 0, interval, t -> {
                if (!playing) return;

                nextFrame();

                // 通知更新（直接在玩家线程执行，无需额外调度）
                if (updateCallback != null) {
                    updateCallback.run();
                }
            });
        } else {
            // 回退到全局调度器（兼容旧代码）
            task = FoliaScheduler.runTimerGlobal(0, interval, t -> {
                if (!playing) return;

                nextFrame();

                // 通知更新
                if (updateCallback != null) {
                    updateCallback.run();
                }
            });
        }
    }

    private void stopAnimation() {
        if (task instanceof org.bukkit.scheduler.BukkitTask bukkitTask) {
            bukkitTask.cancel();
        } else if (task instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
            foliaTask.cancel();
        }
        task = null;
    }

    private Plugin getPlugin() {
        com.thenextlvl.foliagui.manager.GUIManager manager = com.thenextlvl.foliagui.manager.GUIManager.getInstance();
        return manager != null ? manager.getPlugin() : null;
    }

    private ItemStack getDefaultItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§c无动画帧");
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return getCurrentFrame();
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        // 动画图标由帧控制
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
        if (clickHandler != null) {
            ClickEvent clickEvent = new ClickEvent(
                event.getPlayer(),
                event.getInventory(),
                event.getSlot(),
                event.getClickType(),
                event.getAction(),
                event.getCurrentItem(),
                event.getCursor()
            );
            clickHandler.accept(clickEvent);
            if (clickEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
    }

    @Override
    public @NotNull AnimatedIcon onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        this.interactable = true;
        return this;
    }

    @Override
    public AnimatedIcon permission(@Nullable String permission) {
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return null;
    }

    @Override
    public AnimatedIcon condition(@Nullable Predicate<Player> condition) {
        return this;
    }

    @Override
    public AnimatedIcon refreshInterval(int ticks) {
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return -1; // AnimatedIcon 有自己的动画逻辑
    }

    @Override
    public @NotNull AnimatedIcon clone() {
        AnimatedIconImpl cloned = new AnimatedIconImpl(id);
        for (ItemStack frame : frames) {
            cloned.frames.add(frame.clone());
        }
        cloned.currentFrame = currentFrame;
        cloned.interval = interval;
        cloned.playMode = playMode;
        cloned.interactable = interactable;
        cloned.movable = movable;
        cloned.frameChangeListener = frameChangeListener;
        cloned.clickHandler = clickHandler;
        return cloned;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }
}