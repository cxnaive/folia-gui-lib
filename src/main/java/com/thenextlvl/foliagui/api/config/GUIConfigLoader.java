package com.thenextlvl.foliagui.api.config;

import com.thenextlvl.foliagui.api.GUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * GUI 配置加载器接口
 *
 * @author TheNextLvl
 */
public interface GUIConfigLoader {

    /**
     * 从文件加载 GUI 配置
     * @param file 配置文件
     * @return GUI 实例
     * @throws IOException 读取异常
     */
    @NotNull GUI load(@NotNull File file) throws IOException;

    /**
     * 从输入流加载 GUI 配置
     * @param inputStream 输入流
     * @return GUI 实例
     * @throws IOException 读取异常
     */
    @NotNull GUI load(@NotNull InputStream inputStream) throws IOException;

    /**
     * 从 Map 加载 GUI 配置
     * @param config 配置 Map
     * @return GUI 实例
     */
    @NotNull GUI loadFromMap(@NotNull Map<String, Object> config);

    /**
     * 从资源文件加载 GUI 配置
     * @param resourcePath 资源路径
     * @param classLoader 类加载器
     * @return GUI 实例，找不到资源返回 null
     */
    @Nullable GUI loadFromResource(@NotNull String resourcePath, @NotNull ClassLoader classLoader);

    /**
     * 支持的文件扩展名
     * @return 扩展名数组
     */
    @NotNull String[] getSupportedExtensions();
}