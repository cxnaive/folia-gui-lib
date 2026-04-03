package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.ToggleButton;
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
 * 开关按钮组件实现
 *
 * @author TheNextLvl
 */
public class ToggleButtonImpl implements ToggleButton {

    private final String id;
    private boolean toggled = false;
    private ItemStack onItem;
    private ItemStack offItem;
    private Supplier<ItemStack> onItemSupplier;
    private Supplier<ItemStack> offItemSupplier;
    private Consumer<ToggleEvent> toggleHandler;
    private Consumer<Boolean> stateChangeListener;
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = -1;
    private int slot = -1;
    private boolean interactable = true;

    public ToggleButtonImpl(@NotNull String id) {
        this.id = id;
    }

    public ToggleButtonImpl(@NotNull String id, @NotNull ItemStack onItem, @NotNull ItemStack offItem) {
        this.id = id;
        this.onItem = onItem;
        this.offItem = offItem;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        if (toggled) {
            if (onItemSupplier != null) {
                return onItemSupplier.get();
            }
            return onItem;
        } else {
            if (offItemSupplier != null) {
                return offItemSupplier.get();
            }
            return offItem;
        }
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        this.onItem = item;
        this.offItem = item;
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
    public void onInteract(@NotNull InteractionEvent event) {
    }

    @Override
    public @NotNull Component clone() {
        ToggleButtonImpl cloned = new ToggleButtonImpl(id, onItem.clone(), offItem.clone());
        cloned.toggled = this.toggled;
        cloned.permission = this.permission;
        cloned.condition = this.condition;
        cloned.refreshInterval = this.refreshInterval;
        cloned.interactable = this.interactable;
        return cloned;
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
    public boolean isToggled() {
        return toggled;
    }

    @Override
    public void setToggled(boolean toggled) {
        boolean oldState = this.toggled;
        this.toggled = toggled;
        if (oldState != toggled && stateChangeListener != null) {
            stateChangeListener.accept(toggled);
        }
    }

    @Override
    public boolean toggle() {
        return toggle(null);
    }

    public boolean toggle(@Nullable Player player) {
        boolean oldState = this.toggled;
        boolean newState = !oldState;
        
        if (toggleHandler != null) {
            ToggleEvent event = new ToggleEvent(this, newState, oldState, player);
            toggleHandler.accept(event);
            if (event.isCancelled()) {
                return oldState;
            }
        }
        
        this.toggled = newState;
        if (stateChangeListener != null) {
            stateChangeListener.accept(newState);
        }
        return newState;
    }

    @Override
    public ToggleButton onItem(@NotNull ItemStack item) {
        this.onItem = item;
        return this;
    }

    @Override
    public ToggleButton offItem(@NotNull ItemStack item) {
        this.offItem = item;
        return this;
    }

    @Override
    public ToggleButton onItem(@NotNull Supplier<ItemStack> supplier) {
        this.onItemSupplier = supplier;
        return this;
    }

    @Override
    public ToggleButton offItem(@NotNull Supplier<ItemStack> supplier) {
        this.offItemSupplier = supplier;
        return this;
    }

    @Override
    public ToggleButton onToggle(@NotNull Consumer<ToggleEvent> handler) {
        this.toggleHandler = handler;
        return this;
    }

    @Override
    public ToggleButton onStateChange(@NotNull Consumer<Boolean> listener) {
        this.stateChangeListener = listener;
        return this;
    }

    @Override
    public ToggleButton onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public ToggleButton permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return condition;
    }

    @Override
    public ToggleButton condition(@Nullable Predicate<Player> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public ToggleButton refreshInterval(int ticks) {
        this.refreshInterval = ticks;
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public boolean isMovable() {
        return false;
    }

    @Override
    public void setMovable(boolean movable) {
    }

    public Consumer<ClickEvent> getClickHandler() {
        return clickHandler;
    }

    public Consumer<ToggleEvent> getToggleHandler() {
        return toggleHandler;
    }
}
