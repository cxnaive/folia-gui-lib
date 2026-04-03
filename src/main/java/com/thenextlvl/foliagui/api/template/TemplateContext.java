package com.thenextlvl.foliagui.api.template;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 模板上下文
 * <p>
 * 包含模板渲染时的所有上下文信息，包括玩家、参数、占位符值等。
 *
 * @author TheNextLvl
 */
public interface TemplateContext {

    /**
     * 获取目标玩家
     * @return 玩家
     */
    @NotNull Player getPlayer();

    /**
     * 获取模板参数
     * @return 参数映射
     */
    @NotNull Map<String, Object> getParameters();

    /**
     * 获取参数值
     * @param name 参数名
     * @return 参数值，如果不存在则返回null
     */
    @Nullable Object getParameter(@NotNull String name);

    /**
     * 获取参数值（带默认值）
     * @param name 参数名
     * @param defaultValue 默认值
     * @return 参数值，如果不存在则返回默认值
     */
    @Nullable Object getParameter(@NotNull String name, @Nullable Object defaultValue);

    /**
     * 获取字符串参数
     * @param name 参数名
     * @return 字符串值，如果不存在则返回null
     */
    default @Nullable String getString(@NotNull String name) {
        Object value = getParameter(name);
        return value != null ? value.toString() : null;
    }

    /**
     * 获取字符串参数（带默认值）
     * @param name 参数名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    default @NotNull String getString(@NotNull String name, @NotNull String defaultValue) {
        String value = getString(name);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取整数参数
     * @param name 参数名
     * @return 整数值，如果不存在或类型不匹配则返回0
     */
    default int getInt(@NotNull String name) {
        Object value = getParameter(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 获取整数参数（带默认值）
     * @param name 参数名
     * @param defaultValue 默认值
     * @return 整数值
     */
    default int getInt(@NotNull String name, int defaultValue) {
        Object value = getParameter(name);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value != null ? Integer.parseInt(value.toString()) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * 获取双精度浮点数参数
     * @param name 参数名
     * @return 双精度值，如果不存在或类型不匹配则返回0.0
     */
    default double getDouble(@NotNull String name) {
        Object value = getParameter(name);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return value != null ? Double.parseDouble(value.toString()) : 0.0;
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 获取布尔参数
     * @param name 参数名
     * @return 布尔值，如果不存在则返回false
     */
    default boolean getBoolean(@NotNull String name) {
        Object value = getParameter(name);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(value.toString());
    }

    /**
     * 获取当前槽位
     * @return 槽位索引
     */
    int getSlot();

    /**
     * 获取当前行
     * @return 行索引（0-based）
     */
    int getRow();

    /**
     * 获取当前列
     * @return 列索引（0-based）
     */
    int getColumn();

    /**
     * 获取布局字符
     * @return 当前位置的布局字符
     */
    char getLayoutChar();

    /**
     * 解析占位符
     * @param text 包含占位符的文本
     * @return 解析后的文本
     */
    @NotNull String resolvePlaceholders(@NotNull String text);

    /**
     * 创建子上下文
     * @param slot 新槽位
     * @param row 新行
     * @param column 新列
     * @param layoutChar 新布局字符
     * @return 新的上下文实例
     */
    @NotNull TemplateContext withPosition(int slot, int row, int column, char layoutChar);
}
