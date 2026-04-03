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

/**
 * 按钮组件实现
 *
 * @author TheNextLvl
 */
public class Button implements Component {

    private final String id;
    private ItemStack displayItem;
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = -1;

    public Button(@NotNull String id, @NotNull ItemStack displayItem) {
        this.id = id;
        this.displayItem = displayItem.clone();
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return displayItem.clone();
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        this.displayItem = item.clone();
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
            // 将 InteractionEvent 转换为 ClickEvent
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
            // 同步取消状态
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
        Button clone = new Button(id, displayItem);
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
