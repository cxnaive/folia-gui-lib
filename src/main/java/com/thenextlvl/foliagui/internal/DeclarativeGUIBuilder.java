package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.annotation.*;
import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.component.DynamicIcon;
import com.thenextlvl.foliagui.api.component.ItemSlot;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.engine.ConditionEngine;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 声明式GUI构建器
 * <p>
 * 解析注解并构建GUI实例，支持：
 * <ul>
 *   <li>@Button, @Icon - 静态按钮和图标</li>
 *   <li>@ItemSlot - 物品槽位</li>
 *   <li>@DynamicIcon - 动态图标</li>
 *   <li>@Layout - 布局定义</li>
 *   <li>@Action, @ActionChain - 动作定义</li>
 * </ul>
 *
 * @author TheNextLvl
 */
public class DeclarativeGUIBuilder {

    private final AbstractDeclarativeGUI declarativeGUI;
    private final Class<?> guiClass;
    private GUIConfig guiConfig;
    private final Map<Character, IconDefinition> iconMap = new HashMap<>();
    private final Map<Integer, ButtonDefinition> buttonMap = new HashMap<>();
    private final Map<String, SlotDefinition> slotDefById = new HashMap<>();  // 用 id 作为键
    private final Map<String, DynamicIconDefinition> dynamicIconDefById = new HashMap<>();  // 用 id 作为键
    private final Map<Integer, SlotDefinition> slotMap = new HashMap<>();  // 用槽位索引作为键（不使用 layout 时）
    private final Map<Integer, DynamicIconDefinition> dynamicIconMap = new HashMap<>();  // 用槽位索引作为键（不使用 layout 时）
    private final Map<String, Method> actionHandlers = new HashMap<>();
    private final Map<String, Method> slotHandlers = new HashMap<>();
    private final Map<String, Method> itemFilters = new HashMap<>();
    private final Map<String, Method> dataProviders = new HashMap<>();
    private final Map<String, Method> conditions = new HashMap<>();
    private String[] layoutLines;
    private char decorationChar = '#';
    private char emptyChar = ' ';

    public DeclarativeGUIBuilder(@NotNull AbstractDeclarativeGUI declarativeGUI) {
        this.declarativeGUI = declarativeGUI;
        this.guiClass = declarativeGUI.getClass();
    }

    public GUI build() {
        Bukkit.getLogger().info("[FoliaGUI] ========== Building GUI: " + guiClass.getSimpleName() + " ==========");
        parseAnnotations();

        Bukkit.getLogger().info("[FoliaGUI] After parseAnnotations:");
        Bukkit.getLogger().info("[FoliaGUI] - layoutLines: " + (layoutLines != null ? java.util.Arrays.toString(layoutLines) : "null"));
        Bukkit.getLogger().info("[FoliaGUI] - slotDefById: " + slotDefById.keySet());
        Bukkit.getLogger().info("[FoliaGUI] - dynamicIconDefById: " + dynamicIconDefById.keySet());
        Bukkit.getLogger().info("[FoliaGUI] - iconMap: " + iconMap.keySet());
        Bukkit.getLogger().info("[FoliaGUI] - slotHandlers: " + slotHandlers.keySet());
        Bukkit.getLogger().info("[FoliaGUI] - itemFilters: " + itemFilters.keySet());

        GUIBuilder builder = GUIBuilder.create(guiConfig.id());

        if (guiConfig.title().length > 0) {
            builder.title(String.join("", guiConfig.title()));
        }
        builder.rows(guiConfig.rows());

        if (layoutLines != null && layoutLines.length > 0) {
            buildFromLayout(builder);
        } else {
            buildFromButtons(builder);
        }

        // 注册动作处理器
        for (Map.Entry<String, Method> entry : actionHandlers.entrySet()) {
            builder.actionHandler(entry.getKey(), (player, args) -> {
                try {
                    entry.getValue().setAccessible(true);
                    entry.getValue().invoke(declarativeGUI, player, args);
                } catch (Exception e) {
                    Bukkit.getLogger().severe("[FoliaGUI] Failed to invoke action handler: " + entry.getKey());
                    e.printStackTrace();
                }
            });
        }

        builder.onOpen(player -> declarativeGUI.onOpen(player));
        builder.onClose(player -> declarativeGUI.onClose(player));

        return builder.build();
    }

    private void parseAnnotations() {
        guiConfig = guiClass.getAnnotation(GUIConfig.class);
        if (guiConfig == null) {
            throw new IllegalStateException("DeclarativeGUI class must have @GUIConfig annotation: " + guiClass.getName());
        }

        // 解析方法注解
        for (Method method : guiClass.getDeclaredMethods()) {
            parseMethodAnnotations(method);
        }

        // 解析字段注解
        for (Field field : guiClass.getDeclaredFields()) {
            parseFieldAnnotations(field);
        }
    }

    private void parseMethodAnnotations(Method method) {
        // @ActionHandler
        ActionHandler actionHandler = method.getAnnotation(ActionHandler.class);
        if (actionHandler != null) {
            String name = actionHandler.value().isEmpty() ? method.getName() : actionHandler.value();
            actionHandlers.put(name.toLowerCase(), method);
        }

        // @SlotHandler
        SlotHandler slotHandler = method.getAnnotation(SlotHandler.class);
        if (slotHandler != null) {
            slotHandlers.put(slotHandler.value().toLowerCase(), method);
        }

        // @ItemFilter
        ItemFilter itemFilter = method.getAnnotation(ItemFilter.class);
        if (itemFilter != null) {
            itemFilters.put(itemFilter.value().toLowerCase(), method);
        }

        // @DataProviderMethod
        DataProviderMethod dataProvider = method.getAnnotation(DataProviderMethod.class);
        if (dataProvider != null) {
            dataProviders.put(dataProvider.value().toLowerCase(), method);
        }

        // @Condition
        Condition condition = method.getAnnotation(Condition.class);
        if (condition != null) {
            conditions.put(condition.value().toLowerCase(), method);
        }
    }

    private void parseFieldAnnotations(Field field) {
        // @Icon
        Icon[] icons = field.getAnnotationsByType(Icon.class);
        for (Icon icon : icons) {
            iconMap.put(icon.id().charAt(0), new IconDefinition(icon));
        }

        // @Button
        com.thenextlvl.foliagui.annotation.Button[] buttons =
                field.getAnnotationsByType(com.thenextlvl.foliagui.annotation.Button.class);
        for (com.thenextlvl.foliagui.annotation.Button button : buttons) {
            buttonMap.put(button.slot(), new ButtonDefinition(button, field));
        }

        // @ItemSlot
        com.thenextlvl.foliagui.annotation.ItemSlot[] itemSlots =
                field.getAnnotationsByType(com.thenextlvl.foliagui.annotation.ItemSlot.class);
        for (com.thenextlvl.foliagui.annotation.ItemSlot slotAnno : itemSlots) {
            String id = slotAnno.id();
            SlotDefinition def = new SlotDefinition(slotAnno, field, id);
            Bukkit.getLogger().info("[FoliaGUI] Found @ItemSlot id='" + id + "' slot=" + slotAnno.slot());
            if (!id.isEmpty()) {
                slotDefById.put(id, def);  // 用 id 存储（layout 模式）
            }
            if (slotAnno.slot() >= 0) {
                slotMap.put(slotAnno.slot(), def);  // 用槽位索引存储（非 layout 模式）
            }
        }

        // @DynamicIcon
        com.thenextlvl.foliagui.annotation.DynamicIcon[] dynamicIcons =
                field.getAnnotationsByType(com.thenextlvl.foliagui.annotation.DynamicIcon.class);
        for (com.thenextlvl.foliagui.annotation.DynamicIcon dynIcon : dynamicIcons) {
            String id = dynIcon.id();
            DynamicIconDefinition def = new DynamicIconDefinition(dynIcon, field);
            Bukkit.getLogger().info("[FoliaGUI] Found @DynamicIcon id='" + id + "' slot=" + dynIcon.slot() + " dataProvider=" + def.dataProvider);
            if (!id.isEmpty()) {
                dynamicIconDefById.put(id, def);  // 用 id 存储（layout 模式）
            }
            if (dynIcon.slot() >= 0) {
                dynamicIconMap.put(dynIcon.slot(), def);  // 用槽位索引存储（非 layout 模式）
            }
        }

        // @Layout
        Layout layout = field.getAnnotation(Layout.class);
        if (layout != null) {
            this.layoutLines = layout.value();
            this.decorationChar = layout.decorationChar();
            this.emptyChar = layout.emptyChar();
            Bukkit.getLogger().info("[FoliaGUI] Found @Layout on field '" + field.getName() + "': " + java.util.Arrays.toString(layout.value()));
        }
    }

    private void buildFromLayout(GUIBuilder builder) {
        int rows = layoutLines.length;
        Map<Character, List<Integer>> slotMapByChar = new HashMap<>();

        Bukkit.getLogger().info("[FoliaGUI] Building from layout, rows=" + rows);
        Bukkit.getLogger().info("[FoliaGUI] slotDefById keys: " + slotDefById.keySet());
        Bukkit.getLogger().info("[FoliaGUI] dynamicIconDefById keys: " + dynamicIconDefById.keySet());

        for (int row = 0; row < rows; row++) {
            String line = row < layoutLines.length ? layoutLines[row] : "";
            Bukkit.getLogger().info("[FoliaGUI] Layout row " + row + ": '" + line + "'");
            for (int col = 0; col < 9; col++) {
                char c = col < line.length() ? line.charAt(col) : emptyChar;
                int slot = row * 9 + col;

                if (c == emptyChar) {
                    continue;
                }

                if (c == decorationChar) {
                    builder.fillSlot(slot, createDecorationItem());
                    continue;
                }

                slotMapByChar.computeIfAbsent(c, k -> new ArrayList<>()).add(slot);
            }
        }

        Bukkit.getLogger().info("[FoliaGUI] slotMapByChar: " + slotMapByChar);

        // 构建 Icon
        for (Map.Entry<Character, List<Integer>> entry : slotMapByChar.entrySet()) {
            char iconId = entry.getKey();
            List<Integer> slots = entry.getValue();

            IconDefinition iconDef = iconMap.get(iconId);
            if (iconDef != null) {
                Consumer<ClickEvent> handler = null;
                if (iconDef.action != null && iconDef.action.length > 0) {
                    handler = createClickHandler(iconDef.action);
                }

                for (int slot : slots) {
                    ItemStack item = createItemFromIcon(iconDef.icon);
                    if (handler != null) {
                        builder.button(slot, item, handler);
                    } else {
                        builder.decoration(slot, item);
                    }
                }
            }
        }

        // 构建 ItemSlot（根据布局中的 ID）
        for (Map.Entry<Character, List<Integer>> entry : slotMapByChar.entrySet()) {
            String id = String.valueOf(entry.getKey());
            SlotDefinition slotDef = slotDefById.get(id);
            if (slotDef != null) {
                int slot = entry.getValue().get(0);  // 取第一个匹配的槽位
                Bukkit.getLogger().info("[FoliaGUI] Building ItemSlot '" + id + "' at slot " + slot);
                buildItemSlot(builder, slot, slotDef);
            }
        }

        // 构建 DynamicIcon（根据布局中的 ID）
        for (Map.Entry<Character, List<Integer>> entry : slotMapByChar.entrySet()) {
            String id = String.valueOf(entry.getKey());
            DynamicIconDefinition dynamicDef = dynamicIconDefById.get(id);
            if (dynamicDef != null) {
                int slot = entry.getValue().get(0);  // 取第一个匹配的槽位
                Bukkit.getLogger().info("[FoliaGUI] Building DynamicIcon '" + id + "' at slot " + slot);
                buildDynamicIcon(builder, slot, dynamicDef);
            }
        }
    }

    /**
     * 构建 ItemSlot 组件
     */
    private void buildItemSlot(GUIBuilder builder, int slot, SlotDefinition slotDef) {
        ItemSlot itemSlot = builder.itemSlot(slot);

        // 设置占位符
        if (slotDef.placeholder != Material.AIR) {
            ItemStack placeholder = ItemBuilder.of(slotDef.placeholder)
                    .name(slotDef.placeholderName)
                    .lore(slotDef.placeholderLore)
                    .build();
            itemSlot.placeholder(placeholder);
        }

        // 设置自动返还
        itemSlot.autoReturn(slotDef.autoReturn);

        // 设置数量限制
        if (slotDef.stackLimit > 0) {
            itemSlot.stackLimit(slotDef.stackLimit);
        }

        // 设置过滤器
        if (!slotDef.filter.isEmpty()) {
            Method filterMethod = itemFilters.get(slotDef.filter.toLowerCase());
            if (filterMethod != null) {
                itemSlot.itemFilter(item -> {
                    try {
                        filterMethod.setAccessible(true);
                        return (boolean) filterMethod.invoke(declarativeGUI, item);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }
        }

        // 设置物品变化回调
        if (!slotDef.onChange.isEmpty()) {
            Method handler = slotHandlers.get(slotDef.onChange.toLowerCase());
            if (handler != null) {
                itemSlot.onItemChange((previous, current) -> {
                    try {
                        handler.setAccessible(true);
                        handler.invoke(declarativeGUI, declarativeGUI.getViewer(), current, previous);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }

    /**
     * 构建 DynamicIcon 组件
     */
    private void buildDynamicIcon(GUIBuilder builder, int slot, DynamicIconDefinition dynamicDef) {
        DynamicIcon dynamicIcon = builder.dynamicIcon(slot);

        // 设置数据提供器
        if (!dynamicDef.dataProvider.isEmpty()) {
            Method providerMethod = dataProviders.get(dynamicDef.dataProvider.toLowerCase());
            if (providerMethod != null) {
                dynamicIcon.dataProvider(player -> {
                    try {
                        providerMethod.setAccessible(true);
                        return (ItemStack) providerMethod.invoke(declarativeGUI, player);
                    } catch (Exception e) {
                        return null;
                    }
                });
            }
        }

        // 设置更新间隔
        dynamicIcon.updateInterval(dynamicDef.updateInterval);

        // 设置条件
        if (!dynamicDef.condition.isEmpty()) {
            Method conditionMethod = conditions.get(dynamicDef.condition.toLowerCase());
            if (conditionMethod != null) {
                dynamicIcon.condition(player -> {
                    try {
                        conditionMethod.setAccessible(true);
                        return (boolean) conditionMethod.invoke(declarativeGUI, player);
                    } catch (Exception e) {
                        return false;
                    }
                });
            }
        }

        // 设置点击事件
        if (dynamicDef.action != null && dynamicDef.action.length > 0) {
            Consumer<ClickEvent> handler = createClickHandler(dynamicDef.action);
            if (handler != null) {
                dynamicIcon.onClick(handler);
            }
        }
    }

    private void buildFromButtons(GUIBuilder builder) {
        // 构建静态按钮
        for (Map.Entry<Integer, ButtonDefinition> entry : buttonMap.entrySet()) {
            int slot = entry.getKey();
            ButtonDefinition buttonDef = entry.getValue();

            ItemStack item = createItemFromButton(buttonDef.button);

            Consumer<ClickEvent> handler = null;
            if (buttonDef.action != null && buttonDef.action.length > 0) {
                handler = createClickHandler(buttonDef.action);
            }

            builder.button(slot, item, handler);
        }

        // 构建 ItemSlot
        for (Map.Entry<Integer, SlotDefinition> entry : slotMap.entrySet()) {
            int slot = entry.getKey();
            SlotDefinition slotDef = entry.getValue();
            buildItemSlot(builder, slot, slotDef);
        }

        // 构建 DynamicIcon
        for (Map.Entry<Integer, DynamicIconDefinition> entry : dynamicIconMap.entrySet()) {
            int slot = entry.getKey();
            DynamicIconDefinition dynamicDef = entry.getValue();
            buildDynamicIcon(builder, slot, dynamicDef);
        }

        // 添加边框
        if (guiConfig.rows() > 1) {
            builder.border(Material.BLACK_STAINED_GLASS_PANE);
        }
    }

    private ItemStack createItemFromIcon(Icon icon) {
        ItemBuilder itemBuilder = ItemBuilder.of(icon.material())
                .name(icon.name())
                .lore(icon.lore())
                .amount(icon.amount());

        if (icon.glow()) {
            itemBuilder.glow();
        }
        if (icon.customModelData() > 0) {
            itemBuilder.customModelData(icon.customModelData());
        }

        return itemBuilder.build();
    }

    private ItemStack createItemFromButton(com.thenextlvl.foliagui.annotation.Button button) {
        ItemBuilder itemBuilder = ItemBuilder.of(button.material())
                .name(button.name())
                .lore(button.lore())
                .amount(button.amount());

        if (button.glow()) {
            itemBuilder.glow();
        }
        if (button.customModelData() > 0) {
            itemBuilder.customModelData(button.customModelData());
        }

        return itemBuilder.build();
    }

    private ItemStack createDecorationItem() {
        return ItemBuilder.of(Material.BLACK_STAINED_GLASS_PANE)
                .name(" ")
                .build();
    }

    private Consumer<ClickEvent> createClickHandler(String[] actions) {
        return event -> {
            Player player = event.getPlayer();
            for (String action : actions) {
                executeAction(player, action);
            }
            event.setCancelled(true);
        };
    }

    private void executeAction(Player player, String action) {
        if (action == null || action.isEmpty()) return;

        String lowerAction = action.toLowerCase().trim();

        if (lowerAction.equals("close")) {
            player.closeInventory();
            return;
        }

        if (lowerAction.equals("refresh")) {
            declarativeGUI.refresh();
            return;
        }

        if (lowerAction.startsWith("tell ")) {
            player.sendMessage(action.substring(5).replace('&', '§'));
            return;
        }

        if (lowerAction.startsWith("command ")) {
            player.performCommand(action.substring(8));
            return;
        }

        if (lowerAction.startsWith("console ")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action.substring(8));
            return;
        }

        if (lowerAction.startsWith("open ")) {
            String guiId = action.substring(5).trim();
            // TODO: 打开其他 GUI
            return;
        }

        if (lowerAction.startsWith("sound ")) {
            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(action.substring(6).toUpperCase());
                player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {
            }
            return;
        }

        // 自定义动作处理器
        String[] parts = action.split("\\s+", 2);
        String handlerName = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        Method handler = actionHandlers.get(handlerName);
        if (handler != null) {
            try {
                handler.setAccessible(true);
                handler.invoke(declarativeGUI, player, args);
            } catch (Exception e) {
                Bukkit.getLogger().severe("[FoliaGUI] Failed to invoke action handler: " + handlerName);
                e.printStackTrace();
            }
        }
    }

    // ==================== 内部类 ====================

    private static class IconDefinition {
        final Icon icon;
        final String[] action;

        IconDefinition(Icon icon) {
            this.icon = icon;
            this.action = icon.action();
        }
    }

    private static class ButtonDefinition {
        final com.thenextlvl.foliagui.annotation.Button button;
        final Field field;
        final String[] action;

        ButtonDefinition(com.thenextlvl.foliagui.annotation.Button button, Field field) {
            this.button = button;
            this.field = field;
            Action actionAnnotation = field.getAnnotation(Action.class);
            this.action = actionAnnotation != null ? actionAnnotation.value() : null;
        }
    }

    private static class SlotDefinition {
        final com.thenextlvl.foliagui.annotation.ItemSlot slotAnnotation;
        final Field field;
        final String id;
        final Material placeholder;
        final String placeholderName;
        final String[] placeholderLore;
        final boolean autoReturn;
        final int stackLimit;
        final String filter;
        final String onChange;

        SlotDefinition(com.thenextlvl.foliagui.annotation.ItemSlot slotAnnotation, Field field, String id) {
            this.slotAnnotation = slotAnnotation;
            this.field = field;
            this.id = id;
            this.placeholder = slotAnnotation.placeholder();
            this.placeholderName = slotAnnotation.placeholderName();
            this.placeholderLore = slotAnnotation.placeholderLore();
            this.autoReturn = slotAnnotation.autoReturn();
            this.stackLimit = slotAnnotation.stackLimit();
            this.filter = slotAnnotation.filter();
            OnItemChange onChangeAnnotation = field.getAnnotation(OnItemChange.class);
            this.onChange = onChangeAnnotation != null ? onChangeAnnotation.value() : "";
        }
    }

    private static class DynamicIconDefinition {
        final com.thenextlvl.foliagui.annotation.DynamicIcon dynamicIconAnnotation;
        final Field field;
        final String dataProvider;
        final int updateInterval;
        final String condition;
        final String[] action;

        DynamicIconDefinition(com.thenextlvl.foliagui.annotation.DynamicIcon dynamicIconAnnotation, Field field) {
            this.dynamicIconAnnotation = dynamicIconAnnotation;
            this.field = field;
            this.updateInterval = dynamicIconAnnotation.updateInterval();
            this.condition = dynamicIconAnnotation.condition();
            this.action = dynamicIconAnnotation.action();

            DataProvider dataProviderAnnotation = field.getAnnotation(DataProvider.class);
            this.dataProvider = dataProviderAnnotation != null ? dataProviderAnnotation.value() : "";
        }
    }
}