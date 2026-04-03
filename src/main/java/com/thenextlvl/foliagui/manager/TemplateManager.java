package com.thenextlvl.foliagui.manager;

import com.thenextlvl.foliagui.api.template.GUITemplate;
import com.thenextlvl.foliagui.internal.template.GUITemplateImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * GUI 模板管理器
 * <p>
 * 管理模板注册、查找和创建
 *
 * @author TheNextLvl
 */
public final class TemplateManager {

    private static TemplateManager instance;
    private final Map<String, GUITemplate> templates = new HashMap<>();

    private TemplateManager() {}

    /**
     * 获取实例
     */
    public static synchronized TemplateManager getInstance() {
        if (instance == null) {
            instance = new TemplateManager();
        }
        return instance;
    }

    /**
     * 注册模板
     * @param template 模板
     */
    public void register(@NotNull GUITemplate template) {
        templates.put(template.getId().toLowerCase(Locale.ROOT), template);
    }

    /**
     * 创建并注册新模板
     * @param id 模板ID
     * @return 模板实例
     */
    public GUITemplate createTemplate(@NotNull String id) {
        GUITemplateImpl template = new GUITemplateImpl(id);
        register(template);
        return template;
    }

    /**
     * 获取模板
     * @param id 模板ID
     * @return 模板，不存在则返回 null
     */
    @Nullable
    public GUITemplate getTemplate(@NotNull String id) {
        return templates.get(id.toLowerCase(Locale.ROOT));
    }

    /**
     * 检查模板是否存在
     * @param id 模板ID
     * @return 是否存在
     */
    public boolean hasTemplate(@NotNull String id) {
        return templates.containsKey(id.toLowerCase(Locale.ROOT));
    }

    /**
     * 注销模板
     * @param id 模板ID
     * @return 被移除的模板
     */
    @Nullable
    public GUITemplate unregister(@NotNull String id) {
        return templates.remove(id.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取所有模板ID
     * @return 模板ID集合
     */
    @NotNull
    public Collection<String> getTemplateIds() {
        return templates.keySet();
    }

    /**
     * 获取所有模板
     * @return 模板集合
     */
    @NotNull
    public Collection<GUITemplate> getTemplates() {
        return templates.values();
    }

    /**
     * 清除所有模板
     */
    public void clear() {
        templates.clear();
    }

    /**
     * 模板构建器
     */
    public static class Builder {
        private final GUITemplateImpl template;

        public Builder(@NotNull String id) {
            this.template = new GUITemplateImpl(id);
        }

        public Builder name(@NotNull String name) {
            template.name(name);
            return this;
        }

        public Builder description(@NotNull String description) {
            template.description(description);
            return this;
        }

        public Builder rows(int rows) {
            template.rows(rows);
            return this;
        }

        public Builder title(@NotNull String title) {
            template.title(title);
            return this;
        }

        public Builder layout(@NotNull String... layout) {
            template.layout(layout);
            return this;
        }

        public Builder parameter(@NotNull String name, @NotNull Class<?> type, boolean required, @Nullable Object defaultValue) {
            template.parameter(name, type, required, defaultValue);
            return this;
        }

        public Builder register() {
            TemplateManager.getInstance().register(template);
            return this;
        }

        public GUITemplate build() {
            return template;
        }
    }
}