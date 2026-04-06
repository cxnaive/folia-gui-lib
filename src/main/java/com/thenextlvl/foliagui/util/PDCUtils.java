package com.thenextlvl.foliagui.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PDC (Persistent Data Container) 工具类
 * <p>
 * 提供简便的方法来读取和写入物品的 PDC 数据
 *
 * @author TheNextLvl
 */
public final class PDCUtils {

    private PDCUtils() {
        // 工具类不允许实例化
    }

    /**
     * 获取 PDC 字符串数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 字符串值，如果不存在则返回 null
     */
    @Nullable
    public static String getString(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        return pdc.get(namespacedKey, PersistentDataType.STRING);
    }

    /**
     * 获取 PDC 整数数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 整数值，如果不存在则返回 null
     */
    @Nullable
    public static Integer getInt(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        return pdc.get(namespacedKey, PersistentDataType.INTEGER);
    }

    /**
     * 获取 PDC 布尔数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 布尔值，如果不存在则返回 false
     */
    public static boolean getBoolean(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        Byte value = pdc.get(namespacedKey, PersistentDataType.BYTE);
        return value != null && value == 1;
    }

    /**
     * 获取 PDC 双精度浮点数数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 双精度浮点数值，如果不存在则返回 null
     */
    @Nullable
    public static Double getDouble(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        return pdc.get(namespacedKey, PersistentDataType.DOUBLE);
    }

    /**
     * 获取 PDC 长整数数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 长整数值，如果不存在则返回 null
     */
    @Nullable
    public static Long getLong(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        return pdc.get(namespacedKey, PersistentDataType.LONG);
    }

    /**
     * 检查是否有 PDC 数据
     *
     * @param item 物品
     * @param key 键名
     * @param type 数据类型
     * @param namespace 命名空间
     * @return 是否存在
     */
    public static boolean has(@NotNull ItemStack item, @NotNull String key,
                               @NotNull PersistentDataType<?, ?> type, @NotNull String namespace) {
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        return pdc.has(namespacedKey, type);
    }

    /**
     * 检查是否有 PDC 字符串数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 是否存在
     */
    public static boolean hasString(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        return has(item, key, PersistentDataType.STRING, namespace);
    }

    /**
     * 检查是否有 PDC 整数数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     * @return 是否存在
     */
    public static boolean hasInt(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        return has(item, key, PersistentDataType.INTEGER, namespace);
    }

    /**
     * 获取 PDC 数据（使用 NamespacedKey）
     *
     * @param item 物品
     * @param key NamespacedKey
     * @param type 数据类型
     * @return 值，如果不存在则返回 null
     */
    @Nullable
    public static <T, Z> Z get(@NotNull ItemStack item, @NotNull NamespacedKey key,
                               @NotNull PersistentDataType<T, Z> type) {
        if (!item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        return pdc.get(key, type);
    }

    /**
     * 设置 PDC 数据
     *
     * @param item 物品
     * @param key 键名
     * @param type 数据类型
     * @param value 值
     * @param namespace 命名空间
     */
    public static <T, Z> void set(@NotNull ItemStack item, @NotNull String key,
                                  @NotNull PersistentDataType<T, Z> type, @NotNull Z value,
                                  @NotNull String namespace) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        pdc.set(namespacedKey, type, value);
        item.setItemMeta(meta);
    }

    /**
     * 设置 PDC 字符串数据
     *
     * @param item 物品
     * @param key 键名
     * @param value 字符串值
     * @param namespace 命名空间
     */
    public static void setString(@NotNull ItemStack item, @NotNull String key,
                                 @NotNull String value, @NotNull String namespace) {
        set(item, key, PersistentDataType.STRING, value, namespace);
    }

    /**
     * 设置 PDC 整数数据
     *
     * @param item 物品
     * @param key 键名
     * @param value 整数值
     * @param namespace 命名空间
     */
    public static void setInt(@NotNull ItemStack item, @NotNull String key,
                              int value, @NotNull String namespace) {
        set(item, key, PersistentDataType.INTEGER, value, namespace);
    }

    /**
     * 设置 PDC 布尔数据
     *
     * @param item 物品
     * @param key 键名
     * @param value 布尔值
     * @param namespace 命名空间
     */
    public static void setBoolean(@NotNull ItemStack item, @NotNull String key,
                                  boolean value, @NotNull String namespace) {
        set(item, key, PersistentDataType.BYTE, (byte) (value ? 1 : 0), namespace);
    }

    /**
     * 移除 PDC 数据
     *
     * @param item 物品
     * @param key 键名
     * @param namespace 命名空间
     */
    public static void remove(@NotNull ItemStack item, @NotNull String key, @NotNull String namespace) {
        if (!item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(namespace, key);
        pdc.remove(namespacedKey);
        item.setItemMeta(meta);
    }
}