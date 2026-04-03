package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.component.*;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 组件库管理器
 * <p>
 * 管理所有预设组件
 * 使用 EditorSession 状态保护机制
 *
 * @author TheNextLvl
 */
public class ComponentLibrary {

    private final FoliaGUIToolkit plugin;

    // 组件定义
    private static final List<ComponentDefinition> COMPONENTS = new ArrayList<>();

    static {
        // 基础组件
        COMPONENTS.add(new ComponentDefinition(
                "button",
                "§e基础按钮",
                Material.STONE_BUTTON,
                Arrays.asList("§7最基础的按钮组件", "§7支持点击事件"),
                ComponentType.BUTTON
        ));

        COMPONENTS.add(new ComponentDefinition(
                "toggle_button",
                "§b开关按钮",
                Material.LEVER,
                Arrays.asList("§7可切换状态的按钮", "§7支持两种显示状态"),
                ComponentType.TOGGLE_BUTTON
        ));

        COMPONENTS.add(new ComponentDefinition(
                "pagination",
                "§a分页组件",
                Material.BOOKSHELF,
                Arrays.asList("§7自动分页显示大量物品", "§7支持上/下页导航"),
                ComponentType.PAGINATION
        ));

        COMPONENTS.add(new ComponentDefinition(
                "progress_bar",
                "§c进度条",
                Material.EXPERIENCE_BOTTLE,
                Arrays.asList("§7显示进度的组件", "§7支持多种样式"),
                ComponentType.PROGRESS_BAR
        ));

        COMPONENTS.add(new ComponentDefinition(
                "slider",
                "§d滑块",
                Material.HOPPER,
                Arrays.asList("§7可拖动调整数值", "§7支持范围设置"),
                ComponentType.SLIDER
        ));

        COMPONENTS.add(new ComponentDefinition(
                "animated_icon",
                "§e动画图标",
                Material.CLOCK,
                Arrays.asList("§7帧动画组件", "§7支持多种播放模式"),
                ComponentType.ANIMATED_ICON
        ));

        COMPONENTS.add(new ComponentDefinition(
                "input_field",
                "§b输入框",
                Material.ANVIL,
                Arrays.asList("§7接收玩家文本输入", "§7支持验证器"),
                ComponentType.INPUT_FIELD
        ));

        COMPONENTS.add(new ComponentDefinition(
                "search_field",
                "§a搜索框",
                Material.SPYGLASS,
                Arrays.asList("§7搜索过滤内容", "§7实时搜索"),
                ComponentType.SEARCH_FIELD
        ));

        COMPONENTS.add(new ComponentDefinition(
                "item_slot",
                "§6物品槽",
                Material.CHEST,
                Arrays.asList("§7可放入物品的槽位", "§7关闭时自动返还"),
                ComponentType.ITEM_SLOT
        ));

        COMPONENTS.add(new ComponentDefinition(
                "sort_button",
                "§9排序按钮",
                Material.REPEATER,
                Arrays.asList("§7点击切换排序方式", "§7支持升降序"),
                ComponentType.SORT_BUTTON
        ));
    }

    public ComponentLibrary(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
    }

    /**
     * 打开组件库界面
     * <p>
     * 使用 EditorSession 状态保护机制
     *
     * @param player  玩家
     * @param session 编辑会话
     */
    public void openLibrary(@NotNull Player player, @NotNull EditorSession session) {
        GUIBuilder builder = GUIBuilder.create("component-library")
                .title("§b组件库")
                .rows(6)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        int slot = 10;
        for (ComponentDefinition def : COMPONENTS) {
            builder.button(slot, createComponentItem(def), event -> {
                event.setCancelled(true);
                handleComponentSelect(player, session, def);
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        // 说明按钮
        builder.button(49, ItemBuilder.of(Material.KNOWLEDGE_BOOK)
                .name("§e使用说明")
                .lore("§7点击组件即可选择", "§7选择后可应用到编辑器")
                .build(), event -> event.setCancelled(true));

        // 返回按钮 - 使用 EditorSession 返回
        builder.button(45, ItemBuilder.of(Material.ARROW)
                .name("§c返回编辑器")
                .lore("§7返回编辑界面")
                .build(), event -> {
            event.setCancelled(true);
            session.returnFromSubMenu();
        });

        GUI libraryGUI = builder.build();
        session.openSubMenu(libraryGUI);
    }

    /**
     * 创建组件显示物品
     */
    private ItemStack createComponentItem(ComponentDefinition def) {
        return ItemBuilder.of(def.material)
                .name(def.name)
                .lore(def.description)
                .lore("")
                .lore("§e点击选择")
                .build();
    }

    /**
     * 处理组件选择
     */
    private void handleComponentSelect(@NotNull Player player, @NotNull EditorSession session,
                                        @NotNull ComponentDefinition def) {
        player.sendMessage("§a已选择组件: " + def.getName());
        player.sendMessage("§7组件类型: " + def.getType().name());

        // TODO: 实现组件应用到编辑器的逻辑
        // 1. 让玩家选择要放置的槽位
        // 2. 创建组件实例并添加到编辑器 GUI

        session.returnFromSubMenu();
    }

    /**
     * 根据类型创建组件实例
     */
    public Component createComponent(ComponentType type) {
        String id = UUID.randomUUID().toString();
        return switch (type) {
            case BUTTON -> new com.thenextlvl.foliagui.internal.Button(id, new ItemStack(Material.STONE));
            case TOGGLE_BUTTON -> new com.thenextlvl.foliagui.internal.ToggleButtonImpl(id,
                    new ItemStack(Material.LIME_DYE), new ItemStack(Material.RED_DYE));
            case PROGRESS_BAR -> new com.thenextlvl.foliagui.internal.ProgressBarImpl(id);
            case SLIDER -> new com.thenextlvl.foliagui.internal.SliderImpl(id);
            case ANIMATED_ICON -> new com.thenextlvl.foliagui.internal.AnimatedIconImpl(id);
            case INPUT_FIELD -> new com.thenextlvl.foliagui.internal.InputFieldImpl(id);
            case SEARCH_FIELD -> new com.thenextlvl.foliagui.internal.SearchFieldImpl(id);
            case ITEM_SLOT -> new com.thenextlvl.foliagui.internal.ItemSlotImpl(id);
            case SORT_BUTTON -> new com.thenextlvl.foliagui.internal.SortButtonImpl(id);
            case PAGINATION -> null; // 分页组件需要特殊处理
        };
    }

    /**
     * 组件定义
     */
    public static class ComponentDefinition {
        private final String id;
        private final String name;
        private final Material material;
        private final List<String> description;
        private final ComponentType type;

        public ComponentDefinition(String id, String name, Material material, List<String> description, ComponentType type) {
            this.id = id;
            this.name = name;
            this.material = material;
            this.description = description;
            this.type = type;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public Material getMaterial() { return material; }
        public List<String> getDescription() { return description; }
        public ComponentType getType() { return type; }
    }

    /**
     * 组件类型
     */
    public enum ComponentType {
        BUTTON,
        TOGGLE_BUTTON,
        PAGINATION,
        PROGRESS_BAR,
        SLIDER,
        ANIMATED_ICON,
        INPUT_FIELD,
        SEARCH_FIELD,
        ITEM_SLOT,
        SORT_BUTTON
    }
}