package com.thenextlvl.foliagui.api.config;

import com.thenextlvl.foliagui.api.GUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * GUI 配置重载器接口
 * 支持运行时重载配置
 *
 * @author TheNextLvl
 */
public interface GUIConfigReloader {

    /**
     * 重载指定 GUI 的配置
     * @param guiId GUI ID
     * @return 是否重载成功
     */
    boolean reload(@NotNull String guiId);

    /**
     * 重载所有 GUI 配置
     */
    void reloadAll();

    /**
     * 注册配置文件
     * @param guiId GUI ID
     * @param configFile 配置文件
     */
    void registerConfigFile(@NotNull String guiId, @NotNull File configFile);

    /**
     * 注销配置文件
     * @param guiId GUI ID
     */
    void unregisterConfigFile(@NotNull String guiId);

    /**
     * 获取配置文件
     * @param guiId GUI ID
     * @return 配置文件，不存在则返回 null
     */
    @Nullable File getConfigFile(@NotNull String guiId);

    /**
     * 获取所有已注册的 GUI ID
     * @return GUI ID 集合
     */
    @NotNull Collection<String> getRegisteredGuiIds();

    /**
     * 检查配置文件是否有更新
     * @param guiId GUI ID
     * @return 是否有更新
     */
    boolean hasUpdate(@NotNull String guiId);

    /**
     * 启用自动重载
     * @param intervalTicks 检查间隔（tick）
     */
    void enableAutoReload(long intervalTicks);

    /**
     * 禁用自动重载
     */
    void disableAutoReload();

    /**
     * 是否启用自动重载
     * @return 是否启用
     */
    boolean isAutoReloadEnabled();
}