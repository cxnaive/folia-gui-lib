package com.thenextlvl.foliagui.internal.template;

import com.thenextlvl.foliagui.api.template.TemplateContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 模板上下文实现
 *
 * @author TheNextLvl
 */
public class TemplateContextImpl implements TemplateContext {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("%([^%]+)%");

    private final Player player;
    private final Map<String, Object> parameters;
    private final Map<String, Function<TemplateContext, String>> placeholders;
    private final int slot;
    private final int row;
    private final int column;
    private final char layoutChar;

    public TemplateContextImpl(
            @NotNull Player player,
            @NotNull Map<String, Object> parameters,
            @NotNull Map<String, Function<TemplateContext, String>> placeholders,
            int slot, int row, int column, char layoutChar) {
        this.player = player;
        this.parameters = parameters;
        this.placeholders = placeholders;
        this.slot = slot;
        this.row = row;
        this.column = column;
        this.layoutChar = layoutChar;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Map<String, Object> getParameters() {
        return parameters;
    }

    @Override
    public @Nullable Object getParameter(@NotNull String name) {
        return parameters.get(name);
    }

    @Override
    public @Nullable Object getParameter(@NotNull String name, @Nullable Object defaultValue) {
        return parameters.getOrDefault(name, defaultValue);
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public char getLayoutChar() {
        return layoutChar;
    }

    @Override
    public @NotNull String resolvePlaceholders(@NotNull String text) {
        if (text.isEmpty()) {
            return text;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1).toLowerCase();
            String replacement = resolvePlaceholder(placeholder);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String resolvePlaceholder(String placeholder) {
        // 检查自定义占位符
        Function<TemplateContext, String> resolver = placeholders.get(placeholder);
        if (resolver != null) {
            try {
                return resolver.apply(this);
            } catch (Exception e) {
                return "%" + placeholder + "%";
            }
        }

        // 内置占位符
        switch (placeholder) {
            case "player":
                return player.getName();
            case "player_uuid":
                return player.getUniqueId().toString();
            case "slot":
                return String.valueOf(slot);
            case "row":
                return String.valueOf(row + 1);
            case "column":
                return String.valueOf(column + 1);
            case "layout_char":
                return String.valueOf(layoutChar);
            default:
                // 检查是否是参数引用
                if (parameters.containsKey(placeholder)) {
                    Object value = parameters.get(placeholder);
                    return value != null ? value.toString() : "";
                }
                return "%" + placeholder + "%";
        }
    }

    @Override
    public @NotNull TemplateContext withPosition(int slot, int row, int column, char layoutChar) {
        return new TemplateContextImpl(player, parameters, placeholders, slot, row, column, layoutChar);
    }
}
