package com.thenextlvl.foliagui.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * ItemStack构建器 - 流畅的API创建物品
 * 
 * @author TheNextLvl
 */
public class ItemBuilder {
    
    private final ItemStack item;
    private ItemMeta meta;
    
    private ItemBuilder(@NotNull Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    
    private ItemBuilder(@NotNull ItemStack item) {
        this.item = item.clone();
        this.meta = this.item.getItemMeta();
    }
    
    /**
     * 从材质创建
     * @param material 材质
     * @return ItemBuilder
     */
    public static ItemBuilder of(@NotNull Material material) {
        return new ItemBuilder(material);
    }
    
    /**
     * 从现有ItemStack创建
     * @param item 物品
     * @return ItemBuilder
     */
    public static ItemBuilder of(@NotNull ItemStack item) {
        return new ItemBuilder(item);
    }
    
    /**
     * 设置显示名称
     * @param name 名称（支持颜色代码&）
     * @return 此构建器
     */
    public ItemBuilder name(@Nullable String name) {
        if (meta != null && name != null) {
            meta.setDisplayName(name.replace('&', '§'));
        }
        return this;
    }
    
    /**
     * 设置描述
     * @param lore 描述（支持颜色代码&）
     * @return 此构建器
     */
    public ItemBuilder lore(@Nullable String... lore) {
        if (meta != null && lore != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace('&', '§'));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }
    
    /**
     * 设置描述（列表形式）
     * @param lore 描述列表
     * @return 此构建器
     */
    public ItemBuilder lore(@Nullable List<String> lore) {
        if (meta != null && lore != null) {
            List<String> coloredLore = new ArrayList<>();
            for (String line : lore) {
                coloredLore.add(line.replace('&', '§'));
            }
            meta.setLore(coloredLore);
        }
        return this;
    }
    
    /**
     * 添加一行描述
     * @param line 描述行
     * @return 此构建器
     */
    public ItemBuilder addLore(@NotNull String line) {
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore == null) {
                lore = new ArrayList<>();
            }
            lore.add(line.replace('&', '§'));
            meta.setLore(lore);
        }
        return this;
    }
    
    /**
     * 设置数量
     * @param amount 数量（1-64）
     * @return 此构建器
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(Math.max(1, Math.min(64, amount)));
        return this;
    }
    
    /**
     * 添加附魔
     * @param enchantment 附魔
     * @param level 等级
     * @return 此构建器
     */
    public ItemBuilder enchant(@NotNull Enchantment enchantment, int level) {
        if (meta != null) {
            meta.addEnchant(enchantment, level, true);
        }
        return this;
    }
    
    /**
     * 添加发光效果（无实际附魔）
     * @return 此构建器
     */
    public ItemBuilder glow() {
        if (meta != null) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        return this;
    }
    
    /**
     * 添加ItemFlag
     * @param flags flags
     * @return 此构建器
     */
    public ItemBuilder flags(@NotNull ItemFlag... flags) {
        if (meta != null) {
            meta.addItemFlags(flags);
        }
        return this;
    }
    
    /**
     * 设置皮革颜色
     * @param color 颜色
     * @return 此构建器
     */
    public ItemBuilder color(@NotNull Color color) {
        if (meta instanceof LeatherArmorMeta leatherMeta) {
            leatherMeta.setColor(color);
        }
        return this;
    }
    
    /**
     * 设置玩家头颅
     * @param playerName 玩家名
     * @return 此构建器
     */
    public ItemBuilder skull(@NotNull String playerName) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwner(playerName);
        }
        return this;
    }
    
    /**
     * 设置不可破坏
     * @param unbreakable 是否不可破坏
     * @return 此构建器
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (meta != null) {
            meta.setUnbreakable(unbreakable);
        }
        return this;
    }
    
    /**
     * 设置CustomModelData
     * @param data 数据
     * @return 此构建器
     */
    public ItemBuilder customModelData(int data) {
        if (meta != null) {
            meta.setCustomModelData(data);
        }
        return this;
    }
    
    /**
     * 构建ItemStack
     * @return ItemStack
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item.clone();
    }
}
