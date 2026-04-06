package com.thenextlvl.foliagui.api.content;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * GUI内容管理器 - 用于管理动态内容并提供槽位索引查找
 * <p>
 * 解决的核心问题：在点击事件处理器中，闭包变量在创建时被捕获，
 * 导致后续刷新后无法获取当前槽位对应的最新数据。
 * <p>
 * 使用方法：
 * <pre>
 * ContentManager contentManager = new ContentManager();
 *
 * // 注册动态内容
 * contentManager.register(0, () -> createItem(currentPageData.get(0)));
 * contentManager.register(1, () -> createItem(currentPageData.get(1)));
 *
 * // 在点击处理器中使用动态查找
 * builder.button(0, initialItem, event -> {
 *     // 动态获取当前槽位对应的数据
 *     ItemStack current = contentManager.getContent(0);
 *     // 或者获取关联的数据ID
 *     String dataId = contentManager.getDataId(0);
 *     ...
 * });
 *
 * // 刷新内容（无音效）
 * contentManager.refreshContents(inventory);
 * </pre>
 *
 * @author TheNextLvl
 */
public class ContentManager {

    // 槽位 -> 内容供应器
    private final Map<Integer, Supplier<ItemStack>> contentProviders = new HashMap<>();

    // 槽位 -> 数据ID（用于关联数据）
    private final Map<Integer, String> dataIds = new HashMap<>();

    // 槽位 -> 数据对象（用于关联复杂数据）
    private final Map<Integer, Object> dataObjects = new HashMap<>();

    // 槽位 -> 动态数据查找函数
    private final Map<Integer, Function<Integer, Object>> dynamicLookupFunctions = new HashMap<>();

    // 当前注册的槽位列表（用于分页等场景）
    private final List<Integer> registeredSlots = new ArrayList<>();

    // 槽位范围（用于分页显示）
    private int[] slotRange;

    /**
     * 注册槽位内容供应器
     *
     * @param slot     槽位
     * @param provider 内容供应器
     * @return 此管理器
     */
    @NotNull
    public ContentManager register(int slot, @NotNull Supplier<ItemStack> provider) {
        contentProviders.put(slot, provider);
        if (!registeredSlots.contains(slot)) {
            registeredSlots.add(slot);
        }
        return this;
    }

    /**
     * 注册槽位内容供应器（带数据ID）
     *
     * @param slot     槽位
     * @param dataId   数据ID（用于关联）
     * @param provider 内容供应器
     * @return 此管理器
     */
    @NotNull
    public ContentManager register(int slot, @NotNull String dataId, @NotNull Supplier<ItemStack> provider) {
        contentProviders.put(slot, provider);
        dataIds.put(slot, dataId);
        if (!registeredSlots.contains(slot)) {
            registeredSlots.add(slot);
        }
        return this;
    }

    /**
     * 注册槽位内容供应器（带数据对象）
     *
     * @param slot     槽位
     * @param data     数据对象
     * @param provider 内容供应器
     * @return 此管理器
     */
    @NotNull
    public ContentManager register(int slot, @NotNull Object data, @NotNull Supplier<ItemStack> provider) {
        contentProviders.put(slot, provider);
        dataObjects.put(slot, data);
        if (!registeredSlots.contains(slot)) {
            registeredSlots.add(slot);
        }
        return this;
    }

    /**
     * 设置动态数据查找函数
     * <p>
     * 用于分页场景：点击处理器可以根据当前槽位索引动态查找对应数据
     *
     * @param slot    槽位
     * @param lookup  动态查找函数（参数为槽位索引，返回关联数据）
     * @return 此管理器
     */
    @NotNull
    public ContentManager setDynamicLookup(int slot, @NotNull Function<Integer, Object> lookup) {
        dynamicLookupFunctions.put(slot, lookup);
        return this;
    }

    /**
     * 设置槽位范围（用于分页）
     *
     * @param slots 槽位数组
     * @return 此管理器
     */
    @NotNull
    public ContentManager setSlotRange(int... slots) {
        this.slotRange = slots;
        return this;
    }

    /**
     * 获取槽位范围
     *
     * @return 槽位数组
     */
    @Nullable
    public int[] getSlotRange() {
        return slotRange;
    }

    /**
     * 批量注册槽位内容（用于分页）
     * <p>
     * 根据数据列表自动注册槽位，并设置动态查找
     *
     * @param slots      槽位数组
     * @param dataList   数据列表
     * @param startIndex 数据起始索引（用于分页）
     * @param renderer   渲染函数
     * @return 此管理器
     */
    @NotNull
    public <T> ContentManager registerPage(@NotNull int[] slots, @NotNull List<T> dataList,
                                            int startIndex, @NotNull Function<T, ItemStack> renderer) {
        this.slotRange = slots;

        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            int dataIndex = startIndex + i;

            // 设置动态查找：点击时根据槽位动态获取数据
            setDynamicLookup(slot, s -> {
                int idx = findSlotIndex(s) + startIndex;
                if (idx >= 0 && idx < dataList.size()) {
                    return dataList.get(idx);
                }
                return null;
            });

            // 注册内容供应器
            register(slot, () -> {
                int idx = dataIndex;
                if (idx >= 0 && idx < dataList.size()) {
                    return renderer.apply(dataList.get(idx));
                }
                return null;
            });
        }

        return this;
    }

    /**
     * 获取槽位在slotRange中的索引
     *
     * @param slot 槽位
     * @return 索引，如果不在范围内返回-1
     */
    private int findSlotIndex(int slot) {
        if (slotRange == null) return -1;
        for (int i = 0; i < slotRange.length; i++) {
            if (slotRange[i] == slot) return i;
        }
        return -1;
    }

    /**
     * 获取槽位当前内容
     *
     * @param slot 槽位
     * @return 内容物品，如果没有注册则返回null
     */
    @Nullable
    public ItemStack getContent(int slot) {
        Supplier<ItemStack> provider = contentProviders.get(slot);
        if (provider != null) {
            return provider.get();
        }
        return null;
    }

    /**
     * 获取槽位关联的数据ID
     *
     * @param slot 槽位
     * @return 数据ID，如果没有关联则返回null
     */
    @Nullable
    public String getDataId(int slot) {
        return dataIds.get(slot);
    }

    /**
     * 获取槽位关联的数据对象
     *
     * @param slot 槽位
     * @return 数据对象，如果没有关联则返回null
     */
    @Nullable
    public Object getDataObject(int slot) {
        return dataObjects.get(slot);
    }

    /**
     * 动态获取槽位关联的数据（使用动态查找函数）
     * <p>
     * 这是解决闭包问题的关键方法。在点击处理器中使用此方法
     * 可以获取当前槽位对应的最新数据，而不是创建时的静态数据。
     *
     * @param slot 槽位
     * @return 动态查找的数据对象，如果没有查找函数则返回静态数据对象
     */
    @Nullable
    public Object getDynamicData(int slot) {
        Function<Integer, Object> lookup = dynamicLookupFunctions.get(slot);
        if (lookup != null) {
            return lookup.apply(slot);
        }
        return dataObjects.get(slot);
    }

    /**
     * 获取动态数据（带类型转换）
     *
     * @param slot 槽位
     * @param type 数据类型
     * @return 动态查找的数据，类型不匹配返回null
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getDynamicData(int slot, @NotNull Class<T> type) {
        Object data = getDynamicData(slot);
        if (data != null && type.isInstance(data)) {
            return (T) data;
        }
        return null;
    }

    /**
     * 刷新所有注册槽位的内容到Inventory（无音效）
     * <p>
     * 直接更新Inventory内容，不重新打开界面，避免播放开关音效。
     *
     * @param inventory 目标Inventory
     */
    public void refreshContents(@NotNull org.bukkit.inventory.Inventory inventory) {
        for (int slot : registeredSlots) {
            ItemStack content = getContent(slot);
            if (content != null) {
                inventory.setItem(slot, content);
            }
        }
    }

    /**
     * 刷新指定槽位的内容到Inventory
     *
     * @param inventory 目标Inventory
     * @param slots     要刷新的槽位
     */
    public void refreshContents(@NotNull org.bukkit.inventory.Inventory inventory, int... slots) {
        for (int slot : slots) {
            ItemStack content = getContent(slot);
            if (content != null) {
                inventory.setItem(slot, content);
            } else {
                // 清空未注册或无内容的槽位
                inventory.setItem(slot, null);
            }
        }
    }

    /**
     * 清除所有注册
     */
    public void clear() {
        contentProviders.clear();
        dataIds.clear();
        dataObjects.clear();
        dynamicLookupFunctions.clear();
        registeredSlots.clear();
        slotRange = null;
    }

    /**
     * 清除指定槽位的注册
     *
     * @param slot 槽位
     */
    public void unregister(int slot) {
        contentProviders.remove(slot);
        dataIds.remove(slot);
        dataObjects.remove(slot);
        dynamicLookupFunctions.remove(slot);
        registeredSlots.remove(Integer.valueOf(slot));
    }

    /**
     * 获取所有已注册的槽位
     *
     * @return 槽位列表
     */
    @NotNull
    public List<Integer> getRegisteredSlots() {
        return new ArrayList<>(registeredSlots);
    }

    /**
     * 检查槽位是否已注册
     *
     * @param slot 槽位
     * @return 是否已注册
     */
    public boolean isRegistered(int slot) {
        return contentProviders.containsKey(slot);
    }

    /**
     * 获取已注册槽位数量
     *
     * @return 槽位数量
     */
    public int getRegisteredCount() {
        return registeredSlots.size();
    }

    /**
     * 更新数据列表（用于分页切换）
     * <p>
     * 更新槽位关联的数据对象，用于分页场景切换页面数据
     *
     * @param slots     槽位数组
     * @param dataList  新数据列表
     * @param startIndex 数据起始索引
     */
    public <T> void updatePageData(int[] slots, List<T> dataList, int startIndex) {
        for (int i = 0; i < slots.length; i++) {
            int slot = slots[i];
            int dataIndex = startIndex + i;

            if (dataIndex >= 0 && dataIndex < dataList.size()) {
                dataObjects.put(slot, dataList.get(dataIndex));
            } else {
                dataObjects.remove(slot);
            }
        }
    }
}