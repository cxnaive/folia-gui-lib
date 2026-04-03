package com.thenextlvl.foliagui.api.sound;

import org.bukkit.Sound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

/**
 * GUI音效配置
 * <p>
 * 管理GUI中各种事件的音效配置
 *
 * @author TheNextLvl
 */
public class SoundConfig {

    private final Map<GUISound.Type, GUISound> sounds = new EnumMap<>(GUISound.Type.class);
    private boolean enabled = true;

    /**
     * 默认音效配置
     */
    public SoundConfig() {
        // 设置默认音效
        setSound(GUISound.Type.OPEN, GUISound.of(Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f));
        setSound(GUISound.Type.CLOSE, GUISound.of(Sound.BLOCK_CHEST_CLOSE, 0.5f, 1.0f));
        setSound(GUISound.Type.CLICK, GUISound.of(Sound.UI_BUTTON_CLICK, 0.3f, 1.0f));
        setSound(GUISound.Type.TOGGLE, GUISound.of(Sound.BLOCK_LEVER_CLICK, 0.3f, 1.0f));
        setSound(GUISound.Type.PAGE_CHANGE, GUISound.of(Sound.ITEM_BOOK_PAGE_TURN, 0.5f, 1.0f));
        setSound(GUISound.Type.ERROR, GUISound.of(Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f));
        setSound(GUISound.Type.SUCCESS, GUISound.of(Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1.2f));
    }

    /**
     * 创建自定义音效配置
     * @return 配置实例
     */
    public static SoundConfig create() {
        return new SoundConfig();
    }

    /**
     * 创建静音配置
     * @return 静音配置实例
     */
    public static SoundConfig silent() {
        SoundConfig config = new SoundConfig();
        config.setEnabled(false);
        return config;
    }

    /**
     * 检查音效是否启用
     * @return 是否启用
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 设置音效是否启用
     * @param enabled 是否启用
     * @return 此配置
     */
    public SoundConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * 获取指定类型的音效
     * @param type 音效类型
     * @return 音效，如果没有则返回null
     */
    public @Nullable GUISound getSound(@NotNull GUISound.Type type) {
        return sounds.get(type);
    }

    /**
     * 设置指定类型的音效
     * @param type 音效类型
     * @param sound 音效
     * @return 此配置
     */
    public SoundConfig setSound(@NotNull GUISound.Type type, @Nullable GUISound sound) {
        if (sound != null) {
            sounds.put(type, sound);
        } else {
            sounds.remove(type);
        }
        return this;
    }

    /**
     * 设置指定类型的音效（简化方法）
     * @param type 音效类型
     * @param sound Bukkit音效
     * @return 此配置
     */
    public SoundConfig setSound(@NotNull GUISound.Type type, @NotNull Sound sound) {
        return setSound(type, GUISound.of(sound));
    }

    /**
     * 设置指定类型的音效（简化方法）
     * @param type 音效类型
     * @param sound Bukkit音效
     * @param volume 音量
     * @param pitch 音调
     * @return 此配置
     */
    public SoundConfig setSound(@NotNull GUISound.Type type, @NotNull Sound sound, float volume, float pitch) {
        return setSound(type, GUISound.of(sound, volume, pitch));
    }

    /**
     * 移除指定类型的音效
     * @param type 音效类型
     * @return 此配置
     */
    public SoundConfig removeSound(@NotNull GUISound.Type type) {
        sounds.remove(type);
        return this;
    }

    /**
     * 检查是否有指定类型的音效
     * @param type 音效类型
     * @return 是否有音效
     */
    public boolean hasSound(@NotNull GUISound.Type type) {
        return sounds.containsKey(type);
    }

    /**
     * 复制配置
     * @return 新的配置实例
     */
    public SoundConfig copy() {
        SoundConfig copy = new SoundConfig();
        copy.enabled = this.enabled;
        copy.sounds.putAll(this.sounds);
        return copy;
    }

    /**
     * 全局默认音效配置
     */
    private static SoundConfig globalDefault = new SoundConfig();

    /**
     * 获取全局默认音效配置
     * @return 默认配置
     */
    public static SoundConfig getGlobalDefault() {
        return globalDefault;
    }

    /**
     * 设置全局默认音效配置
     * @param config 配置
     */
    public static void setGlobalDefault(@NotNull SoundConfig config) {
        globalDefault = config;
    }
}
