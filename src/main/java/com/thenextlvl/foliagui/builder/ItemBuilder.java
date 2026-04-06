package com.thenextlvl.foliagui.builder;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
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
    private String pdcNamespace = "foliagui"; // PDC 命名空间
    
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

    // ==================== PDC (Persistent Data Container) 支持 ====================

    /**
     * 设置 PDC 数据
     * <p>
     * 示例:
     * <pre>
     * ItemBuilder.of(Material.DIAMOND_SWORD)
     *     .pdc("enchantment", PersistentDataType.STRING, "sharpness")
     *     .pdc("level", PersistentDataType.INTEGER, 5)
     *     .build();
     * </pre>
     *
     * @param key 键名（将使用插件默认命名空间）
     * @param type 数据类型
     * @param value 值
     * @return 此构建器
     */
    @NotNull
    public <T, Z> ItemBuilder pdc(@NotNull String key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        if (meta != null) {
            NamespacedKey namespacedKey = getPDCKey(key);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(namespacedKey, type, value);
        }
        return this;
    }

    /**
     * 设置 PDC 数据（使用自定义 NamespacedKey）
     *
     * @param key NamespacedKey
     * @param type 数据类型
     * @param value 值
     * @return 此构建器
     */
    @NotNull
    public <T, Z> ItemBuilder pdc(@NotNull NamespacedKey key, @NotNull PersistentDataType<T, Z> type, @NotNull Z value) {
        if (meta != null) {
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key, type, value);
        }
        return this;
    }

    /**
     * 设置 PDC 字符串数据
     *
     * @param key 键名
     * @param value 字符串值
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcString(@NotNull String key, @NotNull String value) {
        return pdc(key, PersistentDataType.STRING, value);
    }

    /**
     * 设置 PDC 整数数据
     *
     * @param key 键名
     * @param value 整数值
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcInt(@NotNull String key, int value) {
        return pdc(key, PersistentDataType.INTEGER, value);
    }

    /**
     * 设置 PDC 布尔数据
     *
     * @param key 键名
     * @param value 布尔值
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcBoolean(@NotNull String key, boolean value) {
        return pdc(key, PersistentDataType.BYTE, (byte) (value ? 1 : 0));
    }

    /**
     * 设置 PDC 双精度浮点数数据
     *
     * @param key 键名
     * @param value 双精度浮点数值
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcDouble(@NotNull String key, double value) {
        return pdc(key, PersistentDataType.DOUBLE, value);
    }

    /**
     * 设置 PDC 长整数数据
     *
     * @param key 键名
     * @param value 长整数值
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcLong(@NotNull String key, long value) {
        return pdc(key, PersistentDataType.LONG, value);
    }

    /**
     * 移除 PDC 数据
     *
     * @param key 键名
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder removePdc(@NotNull String key) {
        if (meta != null) {
            NamespacedKey namespacedKey = getPDCKey(key);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.remove(namespacedKey);
        }
        return this;
    }

    /**
     * 检查是否有 PDC 数据
     *
     * @param key 键名
     * @param type 数据类型
     * @return 是否存在
     */
    public boolean hasPdc(@NotNull String key, @NotNull PersistentDataType<?, ?> type) {
        if (meta == null) return false;
        NamespacedKey namespacedKey = getPDCKey(key);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.has(namespacedKey, type);
    }

    /**
     * 获取 PDC 数据
     *
     * @param key 键名
     * @param type 数据类型
     * @return 值，如果不存在则返回 null
     */
    @Nullable
    public <T, Z> Z getPdc(@NotNull String key, @NotNull PersistentDataType<T, Z> type) {
        if (meta == null) return null;
        NamespacedKey namespacedKey = getPDCKey(key);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(namespacedKey, type);
    }

    /**
     * 设置 PDC 命名空间
     * <p>
     * 默认使用 "foliagui" 作为命名空间
     * 可以通过此方法自定义命名空间
     *
     * @param namespace 命名空间
     * @return 此构建器
     */
    @NotNull
    public ItemBuilder pdcNamespace(@NotNull String namespace) {
        this.pdcNamespace = namespace;
        return this;
    }

    /**
     * 获取 PDC 键
     *
     * @param key 键名
     * @return NamespacedKey
     */
    @NotNull
    private NamespacedKey getPDCKey(@NotNull String key) {
        return new NamespacedKey(pdcNamespace, key);
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
