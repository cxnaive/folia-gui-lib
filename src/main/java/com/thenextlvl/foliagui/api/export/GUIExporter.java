package com.thenextlvl.foliagui.api.export;

import com.thenextlvl.foliagui.api.GUI;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;

/**
 * GUI 配置导出器接口
 *
 * @author TheNextLvl
 */
public interface GUIExporter {

    /**
     * 获取导出格式名称
     * @return 格式名称（如 "YAML", "JSON", "Java"）
     */
    @NotNull String getFormat();

    /**
     * 获取文件扩展名
     * @return 扩展名（如 "yml", "json", "java"）
     */
    @NotNull String getFileExtension();

    /**
     * 导出 GUI 配置到字符串
     * @param gui GUI 实例
     * @return 配置字符串
     */
    @NotNull String export(@NotNull GUI gui);

    /**
     * 导出 GUI 配置到 Writer
     * @param gui GUI 实例
     * @param writer 输出写入器
     * @throws IOException 写入异常
     */
    void export(@NotNull GUI gui, @NotNull Writer writer) throws IOException;

    /**
     * 获取内容类型（用于文件头）
     * @return 内容类型
     */
    default @NotNull String getContentType() {
        return "text/plain";
    }

    /**
     * 是否支持导入
     * @return 是否支持
     */
    default boolean supportsImport() {
        return false;
    }
}