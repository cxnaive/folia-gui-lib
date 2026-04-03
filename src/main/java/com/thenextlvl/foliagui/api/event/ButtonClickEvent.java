package com.thenextlvl.foliagui.api.event;

import com.thenextlvl.foliagui.api.component.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 按钮点击事件
 * <p>
 * 当玩家点击一个 Button 组件时触发，包含按钮信息
 *
 * @author TheNextLvl
 */
public class ButtonClickEvent extends ClickEvent {

    private final Component button;
    private final int buttonSlot;

    public ButtonClickEvent(@NotNull Player player, @NotNull Inventory inventory,
                            int slot, @NotNull ClickType clickType, @NotNull InventoryAction action,
                            @Nullable ItemStack currentItem, @Nullable ItemStack cursor,
                            @NotNull Component button, int buttonSlot) {
        super(player, inventory, slot, clickType, action, currentItem, cursor);
        this.button = button;
        this.buttonSlot = buttonSlot;
    }

    /**
     * 获取被点击的按钮组件
     * @return 按钮组件
     */
    @NotNull
    public Component getButton() {
        return button;
    }

    /**
     * 获取按钮的 ID
     * @return 按钮 ID
     */
    @NotNull
    public String getButtonId() {
        return button.getId();
    }

    /**
     * 获取按钮所在槽位
     * @return 槽位索引
     */
    public int getButtonSlot() {
        return buttonSlot;
    }

    /**
     * 获取按钮的显示物品
     * @return 显示物品
     */
    @NotNull
    public ItemStack getButtonItem() {
        return button.getDisplayItem();
    }
}