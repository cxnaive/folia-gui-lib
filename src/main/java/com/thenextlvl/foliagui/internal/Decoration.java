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
 * 装饰组件实现
 * 不可交互，仅用于显示
 *
 * @author TheNextLvl
 */
public class Decoration implements Component {

    private final String id;
    private ItemStack displayItem;
    private int slot = -1;
    private boolean interactable = false;
    private boolean movable = false;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = -1;

    public Decoration(@NotNull String id, @NotNull ItemStack displayItem) {
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
        // 装饰组件不处理交互，取消事件（如果不可移动）
        if (!movable) {
            event.setCancelled(true);
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
        Decoration clone = new Decoration(id, displayItem);
        clone.slot = this.slot;
        clone.interactable = this.interactable;
        clone.movable = this.movable;
        clone.permission = this.permission;
        clone.condition = this.condition;
        clone.refreshInterval = this.refreshInterval;
        return clone;
    }

    @Override
    public Component onClick(@NotNull Consumer<ClickEvent> handler) {
        // 装饰组件不支持点击
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

    public @Nullable String getPermission() {
        return permission;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }
}
