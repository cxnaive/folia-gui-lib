package com.thenextlvl.foliagui.internal.template;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.template.GUITemplate;
import com.thenextlvl.foliagui.api.template.TemplateContext;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * GUI模板实现
 *
 * @author TheNextLvl
 */
public class GUITemplateImpl implements GUITemplate {

    private final String id;
    private String name;
    private String description;
    private int rows = 3;
    private String title = "GUI";
    private String[] layout = new String[0];
    private final Map<String, ParameterDef> parameters = new HashMap<>();
    private final Map<String, Function<TemplateContext, String>> placeholders = new HashMap<>();
    private final Map<Character, Function<TemplateContext, ItemStack>> itemProviders = new HashMap<>();
    private final Map<Character, String> actions = new HashMap<>();
    private final Map<String, BiConsumer<Player, String>> actionHandlers = new HashMap<>();
    private GUITemplate parent;

    public GUITemplateImpl(@NotNull String id) {
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

    @Override
    public GUITemplate name(@NotNull String name) {
        this.name = name;
        return this;
    }

    @Override
    public @Nullable String getDescription() {
        return description;
    }

    @Override
    public GUITemplate description(@Nullable String description) {
        this.description = description;
        return this;
    }

    @Override
    public GUITemplate rows(int rows) {
        this.rows = Math.max(1, Math.min(6, rows));
        return this;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public GUITemplate title(@NotNull String title) {
        this.title = title;
        return this;
    }

    @Override
    public @NotNull String getTitle() {
        return title;
    }

    @Override
    public GUITemplate layout(@NotNull String... layout) {
        this.layout = layout;
        return this;
    }

    @Override
    public @NotNull String[] getLayout() {
        return layout.clone();
    }

    @Override
    public GUITemplate parameter(@NotNull String name, @NotNull Class<?> type, boolean required, @Nullable Object defaultValue) {
        parameters.put(name, new ParameterDef(name, type, required, defaultValue));
        return this;
    }

    @Override
    public GUITemplate registerPlaceholder(@NotNull String placeholder, @NotNull Function<TemplateContext, String> resolver) {
        placeholders.put(placeholder.toLowerCase(), resolver);
        return this;
    }

    @Override
    public GUITemplate registerItem(char character, @NotNull Function<TemplateContext, ItemStack> itemProvider) {
        itemProviders.put(character, itemProvider);
        return this;
    }

    @Override
    public GUITemplate registerAction(char character, @NotNull String action) {
        actions.put(character, action);
        return this;
    }

    @Override
    public GUITemplate parent(@Nullable GUITemplate parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public @Nullable GUITemplate getParent() {
        return parent;
    }

    @Override
    public ValidationResult validate(@NotNull Map<String, Object> parameters) {
        for (ParameterDef def : this.parameters.values()) {
            if (def.required && !parameters.containsKey(def.name)) {
                return ValidationResult.failure("Missing required parameter: " + def.name);
            }
            if (parameters.containsKey(def.name)) {
                Object value = parameters.get(def.name);
                if (value != null && !def.type.isInstance(value)) {
                    return ValidationResult.failure("Parameter " + def.name + " must be of type " + def.type.getSimpleName());
                }
            }
        }
        return ValidationResult.success();
    }

    @Override
    public @NotNull GUI create(@NotNull Player player) {
        return create(player, new HashMap<>());
    }

    @Override
    public @NotNull GUI create(@NotNull Player player, @NotNull Map<String, Object> parameters) {
        ValidationResult validation = validate(parameters);
        if (!validation.isValid()) {
            throw new IllegalArgumentException("Template validation failed: " + validation.getErrorMessage());
        }

        // 填充默认值
        Map<String, Object> finalParams = new HashMap<>(parameters);
        for (ParameterDef def : this.parameters.values()) {
            if (!finalParams.containsKey(def.name) && def.defaultValue != null) {
                finalParams.put(def.name, def.defaultValue);
            }
        }

        // 创建上下文
        TemplateContextImpl context = new TemplateContextImpl(player, finalParams, placeholders, 0, 0, 0, ' ');

        // 解析标题
        String resolvedTitle = context.resolvePlaceholders(title);

        // 构建GUI
        GUIBuilder builder = GUIBuilder.create(id + "_" + player.getUniqueId())
                .title(resolvedTitle)
                .rows(rows);

        // 应用布局
        if (layout.length > 0) {
            applyLayout(builder, context);
        }

        return builder.build();
    }

    private void applyLayout(GUIBuilder builder, TemplateContextImpl baseContext) {
        for (int row = 0; row < layout.length && row < rows; row++) {
            String line = layout[row];
            for (int col = 0; col < line.length() && col < 9; col++) {
                char c = line.charAt(col);
                int slot = row * 9 + col;

                TemplateContext context = baseContext.withPosition(slot, row, col, c);

                // 获取物品提供器
                Function<TemplateContext, ItemStack> provider = itemProviders.get(c);
                if (provider != null) {
                    ItemStack item = provider.apply(context);
                    if (item != null) {
                        String action = actions.get(c);
                        if (action != null) {
                            builder.button(slot, item, event -> {
                                BiConsumer<Player, String> handler = actionHandlers.get(action);
                                if (handler != null) {
                                    handler.accept((Player) event.getPlayer(), action);
                                }
                            });
                        } else {
                            builder.decoration(slot, item);
                        }
                    }
                }
            }
        }
    }

    /**
     * 注册动作处理器
     * @param action 动作名称
     * @param handler 处理器
     * @return 此模板
     */
    public GUITemplateImpl actionHandler(@NotNull String action, @NotNull BiConsumer<Player, String> handler) {
        actionHandlers.put(action, handler);
        return this;
    }

    private static class ParameterDef {
        final String name;
        final Class<?> type;
        final boolean required;
        final Object defaultValue;

        ParameterDef(String name, Class<?> type, boolean required, Object defaultValue) {
            this.name = name;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }
    }
}
