package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.ItemSlot;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 物品填充格组件实现
 *
 * @author TheNextLvl
 */
public class ItemSlotImpl implements ItemSlot {

    private final String id;
    private int slot = -1;
    private ItemStack storedItem;
    private ItemStack placeholder;
    private boolean autoReturn = true;
    private int stackLimit = -1; // -1 表示无限制
    private Predicate<ItemStack> itemFilter;
    private Consumer<ItemStack> onItemPlacedCallback;
    private Consumer<ItemStack> onItemRemovedCallback;
    private ItemChangeCallback onItemChangeCallback;
    private boolean interactable = true;
    private boolean movable = false; // ItemSlot 默认不可移动
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;

    public ItemSlotImpl(@NotNull String id) {
        this.id = id;
        // 默认占位符为 LIGHT_GRAY_STAINED_GLASS_PANE
        this.placeholder = ItemBuilder.of(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                .name("§7物品槽位")
                .lore("§e点击放入物品")
                .build();
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        if (storedItem != null && !storedItem.getType().isAir()) {
            return storedItem.clone();
        }
        if (placeholder != null) {
            return placeholder.clone();
        }
        return new ItemStack(Material.AIR);
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        // 对于 ItemSlot，setDisplayItem 相当于设置存储的物品
        setStoredItem(item);
    }

    @Override
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    @Override
    public boolean isMovable() {
        return movable;
    }

    @Override
    public void setMovable(boolean movable) {
        this.movable = movable;
    }

    @Override
    public Component refreshInterval(int ticks) {
        // ItemSlot 不需要自动刷新
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return -1;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public Component onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public Component permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return condition;
    }

    @Override
    public Component condition(@Nullable Predicate<Player> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
        // ItemSlot 的交互由 AbstractGUI 的特殊处理逻辑处理
        // 主要的物品放入/取出逻辑在 AbstractGUI 的 handleItemSlotClick 方法中处理
    }

    @Override
    public @Nullable ItemStack getStoredItem() {
        return storedItem;
    }

    @Override
    public void setStoredItem(@Nullable ItemStack item) {
        ItemStack previous = this.storedItem;
        this.storedItem = item;

        // 触发统一的 onItemChange 回调
        if (onItemChangeCallback != null) {
            onItemChangeCallback.onChange(previous, item);
        }

        // 触发分开的回调（保持向后兼容）
        if (previous != null && onItemRemovedCallback != null) {
            onItemRemovedCallback.accept(previous);
        }
        if (item != null && onItemPlacedCallback != null) {
            onItemPlacedCallback.accept(item);
        }
    }

    @Override
    public @Nullable ItemStack getPlaceholder() {
        return placeholder;
    }

    @Override
    public @NotNull ItemSlot placeholder(@Nullable ItemStack placeholder) {
        this.placeholder = placeholder;
        return this;
    }

    @Override
    public boolean isAutoReturn() {
        return autoReturn;
    }

    @Override
    public @NotNull ItemSlot autoReturn(boolean autoReturn) {
        this.autoReturn = autoReturn;
        return this;
    }

    @Override
    public @NotNull ItemSlot onItemPlaced(@Nullable Consumer<ItemStack> callback) {
        this.onItemPlacedCallback = callback;
        return this;
    }

    @Override
    public @NotNull ItemSlot onItemRemoved(@Nullable Consumer<ItemStack> callback) {
        this.onItemRemovedCallback = callback;
        return this;
    }

    @Override
    public @NotNull ItemSlot onItemChange(@Nullable ItemChangeCallback callback) {
        this.onItemChangeCallback = callback;
        return this;
    }

    @Override
    public @NotNull ItemSlot stackLimit(int limit) {
        this.stackLimit = limit;
        return this;
    }

    @Override
    public int getStackLimit() {
        return stackLimit;
    }

    @Override
    public @NotNull ItemSlot itemFilter(@Nullable Predicate<ItemStack> filter) {
        this.itemFilter = filter;
        return this;
    }

    @Override
    public boolean canAccept(@NotNull ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // 检查物品过滤
        if (itemFilter != null && !itemFilter.test(item)) {
            return false;
        }

        // 检查数量限制
        if (stackLimit > 0 && item.getAmount() > stackLimit) {
            return false;
        }

        return true;
    }

    @Override
    public @Nullable ItemStack clear() {
        ItemStack removed = storedItem;
        storedItem = null;

        // 触发统一的 onItemChange 回调
        if (onItemChangeCallback != null) {
            onItemChangeCallback.onChange(removed, null);
        }

        // 触发 onItemRemoved 回调（保持向后兼容）
        if (removed != null && onItemRemovedCallback != null) {
            onItemRemovedCallback.accept(removed);
        }
        return removed;
    }

    @Override
    public @NotNull ItemSlot clone() {
        ItemSlotImpl clone = new ItemSlotImpl(id);
        clone.slot = this.slot;
        clone.storedItem = storedItem != null ? storedItem.clone() : null;
        clone.placeholder = placeholder != null ? placeholder.clone() : null;
        clone.autoReturn = autoReturn;
        clone.stackLimit = stackLimit;
        clone.itemFilter = itemFilter;
        clone.onItemPlacedCallback = onItemPlacedCallback;
        clone.onItemRemovedCallback = onItemRemovedCallback;
        clone.onItemChangeCallback = onItemChangeCallback;
        clone.interactable = interactable;
        clone.movable = movable;
        clone.clickHandler = clickHandler;
        clone.permission = permission;
        clone.condition = condition;
        return clone;
    }
}