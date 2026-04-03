package com.thenextlvl.foliagui.manager;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.export.GUIExporter;
import com.thenextlvl.foliagui.internal.export.JavaBuilderExporter;
import com.thenextlvl.foliagui.internal.export.JsonExporter;
import com.thenextlvl.foliagui.internal.export.YamlExporter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GUI 导出管理器
 * <p>
 * 管理多种导出格式，支持导出 GUI 配置
 *
 * @author TheNextLvl
 */
public final class GUIExportManager {

    private static GUIExportManager instance;
    private final Map<String, GUIExporter> exporters = new HashMap<>();

    private GUIExportManager() {
        // 注册默认导出器
        registerExporter(new YamlExporter());
        registerExporter(new JsonExporter());
        registerExporter(new JavaBuilderExporter());
    }

    /**
     * 获取实例
     */
    public static synchronized GUIExportManager getInstance() {
        if (instance == null) {
            instance = new GUIExportManager();
        }
        return instance;
    }

    /**
     * 注册导出器
     * @param exporter 导出器
     */
    public void registerExporter(@NotNull GUIExporter exporter) {
        exporters.put(exporter.getFormat().toLowerCase(Locale.ROOT), exporter);
    }

    /**
     * 获取导出器
     * @param format 格式名称
     * @return 导出器，不存在则返回 null
     */
    @Nullable
    public GUIExporter getExporter(@NotNull String format) {
        return exporters.get(format.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取所有支持的格式
     * @return 格式名称集合
     */
    @NotNull
    public Collection<String> getSupportedFormats() {
        return exporters.keySet();
    }

    /**
     * 导出 GUI 到字符串
     * @param gui GUI 实例
     * @param format 格式名称
     * @return 配置字符串
     */
    @NotNull
    public String exportToString(@NotNull GUI gui, @NotNull String format) {
        GUIExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }
        return exporter.export(gui);
    }

    /**
     * 导出 GUI 到文件
     * @param gui GUI 实例
     * @param format 格式名称
     * @param file 目标文件
     * @throws IOException 写入异常
     */
    public void exportToFile(@NotNull GUI gui, @NotNull String format, @NotNull File file) throws IOException {
        GUIExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        // 确保父目录存在
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(file)) {
            exporter.export(gui, writer);
        }
    }

    /**
     * 导出 GUI 到文件（自动选择扩展名）
     * @param gui GUI 实例
     * @param format 格式名称
     * @param directory 目标目录
     * @return 生成的文件
     * @throws IOException 写入异常
     */
    @NotNull
    public File exportToDirectory(@NotNull GUI gui, @NotNull String format, @NotNull File directory) throws IOException {
        GUIExporter exporter = getExporter(format);
        if (exporter == null) {
            throw new IllegalArgumentException("Unsupported format: " + format);
        }

        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = gui.getId() + "." + exporter.getFileExtension();
        File file = new File(directory, fileName);
        exportToFile(gui, format, file);
        return file;
    }
}