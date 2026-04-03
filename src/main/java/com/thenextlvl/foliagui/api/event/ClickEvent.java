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
 * GUI点击事件
 * 
 * @author TheNextLvl
 */
public class ClickEvent implements Cancellable {
    
    private final Player player;
    private final Inventory inventory;
    private final int slot;
    private final ClickType clickType;
    private final InventoryAction action;
    private final ItemStack currentItem;
    private final ItemStack cursor;
    private boolean cancelled = false;
    
    public ClickEvent(@NotNull Player player, @NotNull Inventory inventory, int slot,
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
    
    /**
     * 获取点击的玩家
     */
    @NotNull
    public Player getPlayer() {
        return player;
    }
    
    /**
     * 获取被点击的背包
     */
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
    
    /**
     * 获取点击的槽位
     */
    public int getSlot() {
        return slot;
    }
    
    /**
     * 获取点击类型
     */
    @NotNull
    public ClickType getClickType() {
        return clickType;
    }
    
    /**
     * 获取背包动作
     */
    @NotNull
    public InventoryAction getAction() {
        return action;
    }
    
    /**
     * 获取当前槽位的物品
     */
    @Nullable
    public ItemStack getCurrentItem() {
        return currentItem;
    }
    
    /**
     * 获取光标上的物品
     */
    @Nullable
    public ItemStack getCursor() {
        return cursor;
    }
    
    /**
     * 检查是否是左键点击
     */
    public boolean isLeftClick() {
        return clickType.isLeftClick();
    }
    
    /**
     * 检查是否是右键点击
     */
    public boolean isRightClick() {
        return clickType.isRightClick();
    }
    
    /**
     * 检查是否是Shift点击
     */
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
