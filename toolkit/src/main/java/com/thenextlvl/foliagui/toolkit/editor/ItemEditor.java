package com.thenextlvl.foliagui.toolkit.editor;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.toolkit.FoliaGUIToolkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * 物品编辑器
 * <p>
 * 提供详细的物品属性编辑功能
 * 使用 EditorSession 状态保护机制
 *
 * @author TheNextLvl
 */
public class ItemEditor {

    private final FoliaGUIToolkit plugin;

    public ItemEditor(@NotNull FoliaGUIToolkit plugin) {
        this.plugin = plugin;
    }

    /**
     * 打开物品编辑界面
     *
     * @param player   玩家
     * @param item     要编辑的物品
     * @param session  编辑会话
     * @param onSave   保存回调
     */
    public void openEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                           @NotNull ItemSaveCallback onSave) {
        openMainEditor(player, item, session, onSave);
    }

    /**
     * 主编辑界面
     */
    private void openMainEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                 @NotNull ItemSaveCallback onSave) {
        ItemStack editItem = item.clone();

        GUIBuilder builder = GUIBuilder.create("item-editor")
                .title("§e物品编辑器")
                .rows(6)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // 预览区域 (中间)
        builder.button(13, editItem.clone(), event -> {
            event.setCancelled(true);
            // 点击预览不做事
        });

        // 基础属性编辑
        // 名称编辑
        builder.button(28, ItemBuilder.of(Material.NAME_TAG)
                .name("§e编辑名称")
                .lore("§7当前: §f" + getDisplayName(editItem), "", "§e点击修改")
                .build(), event -> {
            event.setCancelled(true);
            openNameEditor(player, editItem, session, onSave);
        });

        // Lore编辑
        builder.button(29, ItemBuilder.of(Material.WRITABLE_BOOK)
                .name("§e编辑 Lore")
                .lore("§7当前行数: §f" + getLoreLineCount(editItem), "", "§e点击修改")
                .build(), event -> {
            event.setCancelled(true);
            openLoreEditor(player, editItem, session, onSave);
        });

        // 数量编辑
        builder.button(30, ItemBuilder.of(Material.CHEST)
                .name("§e编辑数量")
                .lore("§7当前: §f" + editItem.getAmount(), "", "§e点击修改")
                .build(), event -> {
            event.setCancelled(true);
            openAmountEditor(player, editItem, session, onSave);
        });

        // 材质编辑 (通过选择物品)
        builder.button(31, ItemBuilder.of(Material.GRASS_BLOCK)
                .name("§e更改材质")
                .lore("§7当前: §f" + editItem.getType().name(), "", "§e点击选择材质")
                .build(), event -> {
            event.setCancelled(true);
            openMaterialSelector(player, editItem, session, onSave);
        });

        // 附魔编辑
        builder.button(32, ItemBuilder.of(Material.ENCHANTED_BOOK)
                .name("§d编辑附魔")
                .lore("§7管理物品附魔", "", "§e点击编辑")
                .build(), event -> {
            event.setCancelled(true);
            openEnchantEditor(player, editItem, session, onSave);
        });

        // 物品标志
        builder.button(33, ItemBuilder.of(Material.PAPER)
                .name("§b物品标志")
                .lore("§7隐藏/显示属性", "", "§e点击编辑")
                .build(), event -> {
            event.setCancelled(true);
            openFlagEditor(player, editItem, session, onSave);
        });

        // 特殊效果
        List<String> effectLore = getSpecialEffectLore(editItem);
        effectLore.add("");
        effectLore.add("§e点击切换");
        builder.button(34, ItemBuilder.of(Material.BLAZE_POWDER)
                .name("§c特殊效果")
                .lore(effectLore.toArray(new String[0]))
                .build(), event -> {
            event.setCancelled(true);
            toggleSpecialEffect(player, editItem, session, onSave);
        });

        // 保存按钮
        builder.button(48, ItemBuilder.of(Material.LIME_DYE)
                .name("§a保存")
                .lore("§7保存修改并返回编辑器")
                .build(), event -> {
            event.setCancelled(true);
            onSave.onSave(editItem.clone());
            player.sendMessage("§a物品已保存!");
            session.returnFromSubMenu();
        });

        // 取消按钮
        builder.button(50, ItemBuilder.of(Material.RED_DYE)
                .name("§c取消")
                .lore("§7放弃修改并返回编辑器")
                .build(), event -> {
            event.setCancelled(true);
            player.sendMessage("§c已取消编辑");
            session.returnFromSubMenu();
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 名称编辑界面
     */
    private void openNameEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                 @NotNull ItemSaveCallback onSave) {
        // 使用聊天输入
        player.closeInventory();
        player.sendMessage("§e请在聊天中输入新名称:");
        player.sendMessage("§7当前名称: §f" + getDisplayName(item));
        player.sendMessage("§7输入 §c'cancel' §7取消");
        // TODO: 实现聊天输入监听
    }

    /**
     * Lore编辑界面
     */
    private void openLoreEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                 @NotNull ItemSaveCallback onSave) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        GUIBuilder builder = GUIBuilder.create("item-editor-lore")
                .title("§eLore 编辑器")
                .rows(6)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // 显示当前 Lore
        int slot = 10;
        for (int i = 0; i < Math.min(lore.size(), 27); i++) {
            final int loreIndex = i;
            String line = lore.get(i);
            builder.button(slot, ItemBuilder.of(Material.PAPER)
                    .name("§f行 " + (i + 1))
                    .lore("§7内容: §f" + line, "", "§e左键编辑", "§c右键删除")
                    .build(), event -> {
                event.setCancelled(true);
                if (event.getClickType().isRightClick()) {
                    // 删除
                    lore.remove(loreIndex);
                    meta.setLore(lore.isEmpty() ? null : lore);
                    item.setItemMeta(meta);
                    openLoreEditor(player, item, session, onSave);
                } else {
                    // 编辑
                    player.sendMessage("§e编辑第 " + (loreIndex + 1) + " 行: " + line);
                    // TODO: 聊天输入
                }
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        // 添加新行
        builder.button(45, ItemBuilder.of(Material.EMERALD)
                .name("§a添加新行")
                .build(), event -> {
            event.setCancelled(true);
            lore.add("§7新行");
            meta.setLore(lore);
            item.setItemMeta(meta);
            openLoreEditor(player, item, session, onSave);
        });

        // 清空 Lore
        builder.button(46, ItemBuilder.of(Material.BARRIER)
                .name("§c清空所有")
                .build(), event -> {
            event.setCancelled(true);
            lore.clear();
            meta.setLore(null);
            item.setItemMeta(meta);
            openLoreEditor(player, item, session, onSave);
        });

        // 返回
        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openMainEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 数量编辑界面
     */
    private void openAmountEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                   @NotNull ItemSaveCallback onSave) {
        GUIBuilder builder = GUIBuilder.create("item-editor-amount")
                .title("§e数量编辑")
                .rows(3)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // 预览
        builder.button(13, item.clone(), event -> event.setCancelled(true));

        // -10
        builder.button(10, ItemBuilder.of(Material.RED_STAINED_GLASS_PANE)
                .name("§c-10")
                .build(), event -> {
            event.setCancelled(true);
            modifyAmount(item, -10);
            openAmountEditor(player, item, session, onSave);
        });

        // -1
        builder.button(11, ItemBuilder.of(Material.RED_DYE)
                .name("§c-1")
                .build(), event -> {
            event.setCancelled(true);
            modifyAmount(item, -1);
            openAmountEditor(player, item, session, onSave);
        });

        // +1
        builder.button(15, ItemBuilder.of(Material.LIME_DYE)
                .name("§a+1")
                .build(), event -> {
            event.setCancelled(true);
            modifyAmount(item, 1);
            openAmountEditor(player, item, session, onSave);
        });

        // +10
        builder.button(16, ItemBuilder.of(Material.LIME_STAINED_GLASS_PANE)
                .name("§a+10")
                .build(), event -> {
            event.setCancelled(true);
            modifyAmount(item, 10);
            openAmountEditor(player, item, session, onSave);
        });

        // 设置为最大堆叠
        builder.button(12, ItemBuilder.of(Material.CHEST)
                .name("§e设为最大")
                .lore("§7最大堆叠: " + item.getMaxStackSize())
                .build(), event -> {
            event.setCancelled(true);
            item.setAmount(item.getMaxStackSize());
            openAmountEditor(player, item, session, onSave);
        });

        // 设置为1
        builder.button(14, ItemBuilder.of(Material.PLAYER_HEAD)
                .name("§e设为 1")
                .build(), event -> {
            event.setCancelled(true);
            item.setAmount(1);
            openAmountEditor(player, item, session, onSave);
        });

        // 返回
        builder.button(22, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openMainEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    private void modifyAmount(ItemStack item, int delta) {
        int newAmount = Math.max(1, Math.min(item.getMaxStackSize(), item.getAmount() + delta));
        item.setAmount(newAmount);
    }

    /**
     * 材质选择界面
     */
    private void openMaterialSelector(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                       @NotNull ItemSaveCallback onSave) {
        GUIBuilder builder = GUIBuilder.create("item-editor-material")
                .title("§e选择材质")
                .rows(6)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        // 常用材质
        Material[] commonMaterials = {
                Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT,
                Material.NETHER_STAR, Material.BEACON, Material.CLOCK, Material.COMPASS,
                Material.NAME_TAG, Material.PAPER, Material.BOOK, Material.ENCHANTED_BOOK,
                Material.CHEST, Material.ENDER_CHEST, Material.BARRIER, Material.ARROW,
                Material.BLAZE_POWDER, Material.BLAZE_ROD, Material.CLOCK, Material.COMPARATOR,
                Material.REPEATER, Material.REDSTONE, Material.ENDER_EYE, Material.ENDER_PEARL,
                Material.DIAMOND_SWORD, Material.DIAMOND_PICKAXE, Material.DIAMOND_AXE, Material.DIAMOND_SHOVEL,
                Material.PLAYER_HEAD, Material.CRAFTING_TABLE, Material.FURNACE, Material.ANVIL,
                Material.RED_DYE, Material.GREEN_DYE, Material.BLUE_DYE, Material.YELLOW_DYE
        };

        int slot = 10;
        for (Material material : commonMaterials) {
            builder.button(slot, ItemBuilder.of(material)
                    .name("§f" + material.name())
                    .lore("", "§e点击选择")
                    .build(), event -> {
                event.setCancelled(true);
                item.setType(material);
                openMainEditor(player, item, session, onSave);
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        // 返回
        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openMainEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 附魔编辑界面
     */
    private void openEnchantEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                    @NotNull ItemSaveCallback onSave) {
        GUIBuilder builder = GUIBuilder.create("item-editor-enchant")
                .title("§d附魔编辑")
                .rows(6)
                .border(Material.PURPLE_STAINED_GLASS_PANE);

        ItemMeta meta = item.getItemMeta();
        Map<Enchantment, Integer> enchantments = meta != null ? new HashMap<>(meta.getEnchants()) : new HashMap<>();

        int slot = 10;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchant = entry.getKey();
            int level = entry.getValue();

            builder.button(slot, ItemBuilder.of(Material.ENCHANTED_BOOK)
                    .name("§d" + getEnchantName(enchant))
                    .lore("§7等级: §f" + level, "", "§e左键 +1", "§c右键 -1", "§7Shift+右键删除")
                    .build(), event -> {
                event.setCancelled(true);
                if (event.getClickType().isShiftClick() && event.getClickType().isRightClick()) {
                    // 删除附魔
                    meta.removeEnchant(enchant);
                    item.setItemMeta(meta);
                    openEnchantEditor(player, item, session, onSave);
                } else if (event.getClickType().isRightClick()) {
                    // -1
                    int newLevel = Math.max(0, level - 1);
                    meta.removeEnchant(enchant);
                    if (newLevel > 0) {
                        meta.addEnchant(enchant, newLevel, true);
                    }
                    item.setItemMeta(meta);
                    openEnchantEditor(player, item, session, onSave);
                } else {
                    // +1
                    meta.addEnchant(enchant, Math.min(255, level + 1), true);
                    item.setItemMeta(meta);
                    openEnchantEditor(player, item, session, onSave);
                }
            });
            slot++;
            if (slot == 17) slot = 19;
        }

        // 添加附魔
        builder.button(45, ItemBuilder.of(Material.EMERALD)
                .name("§a添加附魔")
                .build(), event -> {
            event.setCancelled(true);
            openEnchantSelector(player, item, session, onSave);
        });

        // 清空附魔
        builder.button(46, ItemBuilder.of(Material.BARRIER)
                .name("§c清空所有附魔")
                .build(), event -> {
            event.setCancelled(true);
            for (Enchantment enchant : new ArrayList<>(enchantments.keySet())) {
                meta.removeEnchant(enchant);
            }
            item.setItemMeta(meta);
            openEnchantEditor(player, item, session, onSave);
        });

        // 返回
        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openMainEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 附魔选择界面
     */
    private void openEnchantSelector(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                      @NotNull ItemSaveCallback onSave) {
        GUIBuilder builder = GUIBuilder.create("enchant-selector")
                .title("§d选择附魔")
                .rows(6)
                .border(Material.PURPLE_STAINED_GLASS_PANE);

        // 使用 Registry 获取附魔
        List<Enchantment> enchants = new ArrayList<>();
        Registry.ENCHANTMENT.forEach(enchants::add);

        int slot = 10;
        for (Enchantment enchant : enchants) {
            if (slot > 43) break; // 防止越界

            builder.button(slot, ItemBuilder.of(Material.ENCHANTED_BOOK)
                    .name("§d" + getEnchantName(enchant))
                    .lore("", "§e点击添加 (等级 1)")
                    .build(), event -> {
                event.setCancelled(true);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.addEnchant(enchant, 1, true);
                    item.setItemMeta(meta);
                }
                openEnchantEditor(player, item, session, onSave);
            });
            slot++;
            if (slot == 17) slot = 19;
            else if (slot == 26) slot = 28;
            else if (slot == 35) slot = 37;
        }

        builder.button(49, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openEnchantEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 物品标志编辑
     */
    private void openFlagEditor(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                 @NotNull ItemSaveCallback onSave) {
        GUIBuilder builder = GUIBuilder.create("item-editor-flags")
                .title("§b物品标志")
                .rows(4)
                .border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            meta = item.getItemMeta();
        }
        final ItemMeta finalMeta = meta;

        ItemFlag[] flags = ItemFlag.values();
        int slot = 10;
        for (ItemFlag flag : flags) {
            boolean hasFlag = finalMeta.hasItemFlag(flag);
            builder.button(slot, ItemBuilder.of(hasFlag ? Material.LIME_DYE : Material.RED_DYE)
                    .name((hasFlag ? "§a" : "§c") + getFlagName(flag))
                    .lore("", hasFlag ? "§a已启用" : "§c已禁用", "", "§e点击切换")
                    .build(), event -> {
                event.setCancelled(true);
                if (finalMeta.hasItemFlag(flag)) {
                    finalMeta.removeItemFlags(flag);
                } else {
                    finalMeta.addItemFlags(flag);
                }
                item.setItemMeta(finalMeta);
                openFlagEditor(player, item, session, onSave);
            });
            slot++;
            if (slot == 17) slot = 19;
        }

        builder.button(31, ItemBuilder.of(Material.ARROW)
                .name("§c返回")
                .build(), event -> {
            event.setCancelled(true);
            openMainEditor(player, item, session, onSave);
        });

        GUI editorGUI = builder.build();
        session.openSubMenu(editorGUI);
    }

    /**
     * 切换特殊效果
     */
    private void toggleSpecialEffect(@NotNull Player player, @NotNull ItemStack item, @NotNull EditorSession session,
                                      @NotNull ItemSaveCallback onSave) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // 获取 LURE 附魔
        Enchantment lureEnchant = Registry.ENCHANTMENT.get(NamespacedKey.minecraft("lure"));
        if (lureEnchant == null) {
            player.sendMessage("§c无法添加发光效果");
            return;
        }

        // 切换发光效果
        if (meta.hasEnchant(lureEnchant)) {
            meta.removeEnchant(lureEnchant);
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage("§c已移除发光效果");
        } else {
            meta.addEnchant(lureEnchant, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            player.sendMessage("§a已添加发光效果");
        }
        item.setItemMeta(meta);
        openMainEditor(player, item, session, onSave);
    }

    private String getDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item.getType().name();
        return meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();
    }

    private int getLoreLineCount(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return 0;
        return meta.getLore().size();
    }

    private List<String> getSpecialEffectLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (meta != null && meta.hasEnchant(Enchantment.LURE) && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) {
            lore.add("§a✓ 发光效果");
        } else {
            lore.add("§c✗ 发光效果");
        }
        return lore;
    }

    private String getEnchantName(Enchantment enchant) {
        String name = enchant.getKey().getKey();
        return name.substring(0, 1).toUpperCase() + name.substring(1).replace("_", " ");
    }

    private String getFlagName(ItemFlag flag) {
        String name = flag.name().replace("HIDE_", "").replace("_", " ");
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * 物品保存回调
     */
    @FunctionalInterface
    public interface ItemSaveCallback {
        void onSave(@NotNull ItemStack item);
    }
}