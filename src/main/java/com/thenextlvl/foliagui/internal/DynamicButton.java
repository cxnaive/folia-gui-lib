package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 动态按钮组件
 * 每次获取显示物品时都会重新生成
 *
 * @author TheNextLvl
 */
public class DynamicButton implements Component {

    private final String id;
    private final Supplier<ItemStack> itemSupplier;
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = -1;

    public DynamicButton(@NotNull String id, @NotNull Supplier<ItemStack> itemSupplier) {
        this.id = id;
        this.itemSupplier = itemSupplier;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        ItemStack item = itemSupplier.get();
        return item != null ? item.clone() : new ItemStack(org.bukkit.Material.AIR);
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        // 动态组件不支持直接设置物品
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
    public int getSlot() {
        return slot;
    }

    @Override
    public void setSlot(int slot) {
        this.slot = slot;
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
        if (clickHandler != null) {
            ClickEvent clickEvent = new ClickEvent(
                event.getPlayer(),
                event.getInventory(),
                event.getSlot(),
                event.getClickType(),
                event.getAction(),
                event.getCurrentItem(),
                event.getCursor()
            );
            clickHandler.accept(clickEvent);
            if (clickEvent.isCancelled()) {
                event.setCancelled(true);
            }
        }
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
    public @NotNull Component clone() {
        DynamicButton clone = new DynamicButton(id, itemSupplier);
        clone.slot = this.slot;
        clone.interactable = this.interactable;
        clone.movable = this.movable;
        clone.clickHandler = this.clickHandler;
        clone.permission = this.permission;
        clone.condition = this.condition;
        clone.refreshInterval = this.refreshInterval;
        return clone;
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
    public Component refreshInterval(int ticks) {
        this.refreshInterval = ticks;
        return this;
    }

    public @Nullable Consumer<ClickEvent> getClickHandler() {
        return clickHandler;
    }

    public @Nullable String getPermission() {
        return permission;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }
}
