package com.thenextlvl.foliagui.api.sound;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * GUI音效接口
 * <p>
 * 定义GUI中使用的音效，支持自定义音效和音量/音调
 *
 * @author TheNextLvl
 */
public interface GUISound {

    /**
     * 获取Bukkit音效
     * @return 音效
     */
    @NotNull Sound getSound();

    /**
     * 获取音量
     * @return 音量 (0.0 - 1.0)
     */
    float getVolume();

    /**
     * 获取音调
     * @return 音调 (0.5 - 2.0)
     */
    float getPitch();

    /**
     * 播放音效给玩家
     * @param player 玩家
     */
    void play(@NotNull Player player);

    /**
     * 创建音效
     * @param sound Bukkit音效
     * @return 音效实例
     */
    static GUISound of(@NotNull Sound sound) {
        return new GUISoundImpl(sound, 1.0f, 1.0f);
    }

    /**
     * 创建音效（自定义音量和音调）
     * @param sound Bukkit音效
     * @param volume 音量
     * @param pitch 音调
     * @return 音效实例
     */
    static GUISound of(@NotNull Sound sound, float volume, float pitch) {
        return new GUISoundImpl(sound, volume, pitch);
    }

    /**
     * 音效类型
     */
    enum Type {
        /** 打开GUI时 */
        OPEN,
        /** 关闭GUI时 */
        CLOSE,
        /** 点击按钮时 */
        CLICK,
        /** 切换开关时 */
        TOGGLE,
        /** 翻页时 */
        PAGE_CHANGE,
        /** 错误提示 */
        ERROR,
        /** 成功提示 */
        SUCCESS,
        /** 悬停时 */
        HOVER
    }
}

/**
 * 音效实现类
 */
class GUISoundImpl implements GUISound {
    private final Sound sound;
    private final float volume;
    private final float pitch;

    GUISoundImpl(Sound sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
    }

    @Override
    public @NotNull Sound getSound() {
        return sound;
    }

    @Override
    public float getVolume() {
        return volume;
    }

    @Override
    public float getPitch() {
        return pitch;
    }

    @Override
    public void play(@NotNull Player player) {
        player.playSound(player.getLocation(), sound, volume, pitch);
    }
}
