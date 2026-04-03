package com.thenextlvl.foliagui.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 组件交互事件
 *
 * @author TheNextLvl
 */
public class InteractionEvent implements Cancellable {

    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ClickType clickType;
    private final InventoryAction action;
    private final ItemStack currentItem;
    private final ItemStack cursor;
    private boolean cancelled = false;

    public InteractionEvent(@NotNull Player player, @NotNull Inventory inventory, int slot,
                           @NotNull ClickType clickType, @NotNull InventoryAction action,
                           @Nullable ItemStack currentItem, @Nullable ItemStack cursor) {
        this.player = player;
        this.inventory = inventory;
        this.slot = slot;
        this.clickType = clickType;
        this.action = action;
        this.currentItem = currentItem;
        this.cursor = cursor;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    public int getSlot() {
        return slot;
    }

    @NotNull
    public ClickType getClickType() {
        return clickType;
    }

    @NotNull
    public InventoryAction getAction() {
        return action;
    }

    @Nullable
    public ItemStack getCurrentItem() {
        return currentItem;
    }

    @Nullable
    public ItemStack getCursor() {
        return cursor;
    }

    public boolean isLeftClick() {
        return clickType.isLeftClick();
    }

    public boolean isRightClick() {
        return clickType.isRightClick();
    }

    public boolean isShiftClick() {
        return clickType.isShiftClick();
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
