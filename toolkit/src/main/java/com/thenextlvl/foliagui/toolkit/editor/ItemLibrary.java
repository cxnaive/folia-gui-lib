package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 物品库管理器
 * <p>
 * 管理预设物品和自定义图标
 * 使用 EditorSession 状态保护机制
 *
 * @author TheNextLvl
 */
public class ItemLibrary {

    private final FoliaGUIToolkit plugin;

    // 预设物品分类
    private static final Map<String, List<LibraryItem>> PRESETS = new LinkedHashMap<>();

    static {
        // 分割板类
        PRESETS.put("分割板", Arrays.asList(
                createPreset("黑色分割板", Material.BLACK_STAINED_GLASS_PANE, "§8分割板"),
                createPreset("白色分割板", Material.WHITE_STAINED_GLASS_PANE, "§f分割板"),
                createPreset("灰色分割板", Material.GRAY_STAINED_GLASS_PANE, "§7分割板"),
                createPreset("淡灰色分割板", Material.LIGHT_GRAY_STAINED_GLASS_PANE, "§8分割板"),
                createPreset("红色分割板", Material.RED_STAINED_GLASS_PANE, "§c分割板"),
                createPreset("橙色分割板", Material.ORANGE_STAINED_GLASS_PANE, "§6分割板"),
                createPreset("黄色分割板", Material.YELLOW_STAINED_GLASS_PANE, "§e分割板"),
                createPreset("绿色分割板", Material.GREEN_STAINED_GLASS_PANE, "§a分割板"),
                createPreset("青色分割板", Material.CYAN_STAINED_GLASS_PANE, "§b分割板"),
                createPreset("蓝色分割板", Material.BLUE_STAINED_GLASS_PANE, "§9分割板"),
                createPreset("紫色分割板", Material.PURPLE_STAINED_GLASS_PANE, "§d分割板"),
                createPreset("粉色分割板", Material.PINK_STAINED_GLASS_PANE, "§c分割板")
        ));

        // 功能按钮类
        PRESETS.put("功能按钮", Arrays.asList(
                createPreset("返回按钮", Material.ARROW, "§c返回", "§7返回上一级菜单"),
                createPreset("关闭按钮", Material.BARRIER, "§c关闭", "§7关闭当前菜单"),
                createPreset("确认按钮", Material.LIME_DYE, "§a确认", "§7确认操作"),
                createPreset("取消按钮", Material.RED_DYE, "§c取消", "§7取消操作"),
                createPreset("上一页", Material.SPECTRAL_ARROW, "§e上一页", "§7翻到上一页"),
                createPreset("下一页", Material.SPECTRAL_ARROW, "§e下一页", "§7翻到下一页"),
                createPreset("刷新按钮", Material.CLOCK, "§b刷新", "§7刷新当前页面"),
                createPreset("设置按钮", Material.COMPARATOR, "§e设置", "§7打开设置")
        ));

        // 装饰类
        PRESETS.put("装饰", Arrays.asList(
                createPreset("星标", Material.NETHER_STAR, "§e★", "§7星级装饰"),
                createPreset("心形", Material.RED_DYE, "§c❤", "§7心形装饰"),
                createPreset("钻石", Material.DIAMOND, "§b◆", "§7钻石装饰"),
                createPreset("金币", Material.GOLD_INGOT, "§6●", "§7金币装饰"),
                createPreset("绿宝石", Material.EMERALD, "§a●", "§7绿宝石装饰"),
                createPreset("铁锭", Material.IRON_INGOT, "§f●", "§7铁锭装饰"),
                createPreset("火焰", Material.BLAZE_POWDER, "§c🔥", "§7火焰装饰"),
                createPreset("闪电", Material.GOLD_BLOCK, "§e⚡", "§7闪电装饰")
        ));

        // 信息展示类
        PRESETS.put("信息展示", Arrays.asList(
                createPreset("信息图标", Material.PAPER, "§f信息", "§7点击查看详情"),
                createPreset("警告图标", Material.YELLOW_DYE, "§e⚠ 警告", "§7警告信息"),
                createPreset("错误图标", Material.RED_DYE, "§c✖ 错误", "§7错误信息"),
                createPreset("成功图标", Material.LIME_DYE, "§a✓ 成功", "§7操作成功"),
                createPreset("提示图标", Material.CYAN_DYE, "§b💡 提示", "§7提示信息"),
                createPreset("问号图标", Material.BOOK, "§7?", "§7帮助信息")
        ));
    }

    // 玩家自定义物品
    private final Map<UUID, List<LibraryItem>> customItems = new HashMap<>();

    public ItemLibrary(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
    }

    /**
     * 创建预设物品
     */
    private static LibraryItem createPreset(String id, Material material, String name, String... lore) {
        return new LibraryItem(id, ItemBuilder.of(material)
                .name(name)
                .lore(lore)
                .build(), false);
    }

    /**
     * 打开物品库界面
     * <p>
     * 使用 EditorSession 状态保护机制
     *
     * @param player  玩家
     * @param session 编辑会话
     */
    public void openLibrary(@NotNull Player player, @NotNull EditorSession session) {
        openCategoryMenu(player, session);
    }

    /**
     * 打开分类选择菜单
     */
    private void openCategoryMenu(@NotNull Player player, @NotNull EditorSession session) {
        GUIBuilder builder = GUIBuilder.create("item-library-categories")
                .title("§e物品库 - 选择分类")
                .rows(4)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        int slot = 10;
        for (String category : PRESETS.keySet()) {
            builder.button(slot, createCategoryItem(category), event -> {
                event.setCancelled(true);
                openItemsInCategory(player, session, category);
            });
            slot++;
            if (slot == 17) slot = 19;
        }

        // 自定义物品按钮
        builder.button(slot, ItemBuilder.of(Material.PLAYER_HEAD)
                .name("§b自定义物品")
                .lore("§7你保存的自定义图标")
                .build(), event -> {
            event.setCancelled(true);
            openCustomItems(player, session);
        });

        // 返回按钮 - 使用 EditorSession 返回
        builder.button(31, ItemBuilder.of(Material.ARROW)
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
     * 创建分类图标
     */
    private ItemStack createCategoryItem(String category) {
        List<LibraryItem> items = PRESETS.get(category);
        if (items == null || items.isEmpty()) return new ItemStack(Material.CHEST);

        // 使用分类中第一个物品的材质作为图标
        Material material = items.get(0).getItem().getType();
        return ItemBuilder.of(material)
                .name("§e" + category)
                .lore("§7包含 §f" + items.size() + " §7个物品", "", "§e点击查看")
                .build();
    }

    /**
     * 打开分类中的物品
     */
    private void openItemsInCategory(@NotNull Player player, @NotNull EditorSession session,
                                      @NotNull String category) {
        List<LibraryItem> items = PRESETS.get(category);
        if (items == null) return;

        GUIBuilder builder = GUIBuilder.create("item-library-items")
                .title("§e物品库 - " + category)
                .rows(6)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        int slot = 10;
        for (LibraryItem item : items) {
            ItemStack displayItem = item.getItem().clone();
            builder.button(slot, displayItem, event -> {
                event.setCancelled(true);
                // 选择物品，放到玩家光标上
                player.setItemOnCursor(item.getItem().clone());
                player.sendMessage("§a已选择物品，可放置到编辑器中");
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        // 返回分类按钮
        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回分类")
                .build(), event -> {
            event.setCancelled(true);
            openCategoryMenu(player, session);
        });

        GUI libraryGUI = builder.build();
        session.openSubMenu(libraryGUI);
    }

    /**
     * 打开自定义物品管理
     */
    private void openCustomItems(@NotNull Player player, @NotNull EditorSession session) {
        List<LibraryItem> items = customItems.getOrDefault(player.getUniqueId(), new ArrayList<>());

        GUIBuilder builder = GUIBuilder.create("item-library-custom")
                .title("§b自定义物品")
                .rows(6)
                .border(Material.YELLOW_STAINED_GLASS_PANE);

        // 显示自定义物品
        int slot = 10;
        for (LibraryItem item : items) {
            builder.button(slot, item.getItem().clone(), event -> {
                event.setCancelled(true);
                player.setItemOnCursor(item.getItem().clone());
                player.sendMessage("§a已选择物品");
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        // 添加新物品按钮
        builder.button(45, ItemBuilder.of(Material.EMERALD)
                .name("§a添加新物品")
                .lore("§7手持物品后点击添加")
                .build(), event -> {
            event.setCancelled(true);
            // 检查玩家主手是否有物品
            ItemStack handItem = player.getInventory().getItemInMainHand();
            if (handItem != null && !handItem.getType().isAir()) {
                addCustomItem(player, handItem.clone());
                player.sendMessage("§a已添加物品到自定义物品库!");
                openCustomItems(player, session);
            } else {
                player.sendMessage("§c请手持物品后再点击!");
            }
        });

        // 返回分类按钮
        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回分类")
                .build(), event -> {
            event.setCancelled(true);
            openCategoryMenu(player, session);
        });

        GUI libraryGUI = builder.build();
        session.openSubMenu(libraryGUI);
    }

    /**
     * 添加自定义物品
     */
    public void addCustomItem(@NotNull Player player, @NotNull ItemStack item) {
        List<LibraryItem> items = customItems.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        String id = "custom_" + System.currentTimeMillis();
        items.add(new LibraryItem(id, item, true));
    }

    /**
     * 删除自定义物品
     */
    public void removeCustomItem(@NotNull Player player, int index) {
        List<LibraryItem> items = customItems.get(player.getUniqueId());
        if (items != null && index >= 0 && index < items.size()) {
            items.remove(index);
        }
    }

    /**
     * 获取玩家的自定义物品
     */
    public List<LibraryItem> getCustomItems(@NotNull Player player) {
        return customItems.getOrDefault(player.getUniqueId(), new ArrayList<>());
    }

    /**
     * 库物品定义
     */
    public static class LibraryItem {
        private final String id;
        private final ItemStack item;
        private final boolean isCustom;

        public LibraryItem(@NotNull String id, @NotNull ItemStack item, boolean isCustom) {
            this.id = id;
            this.item = item;
            this.isCustom = isCustom;
        }

        public String getId() {
            return id;
        }

        public ItemStack getItem() {
            return item;
        }

        public boolean isCustom() {
            return isCustom;
        }
    }
}