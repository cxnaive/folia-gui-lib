package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.SortButton;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 排序按钮组件实现
 *
 * @author TheNextLvl
 */
public class SortButtonImpl implements SortButton {

    private final String id;
    private SortOrder currentOrder = SortOrder.ASCENDING;
    private ItemStack ascendingItem;
    private ItemStack descendingItem;
    private Consumer<SortChangeEvent> sortChangeListener;
    private Consumer<ClickEvent> clickHandler;
    private boolean movable = false;
    private boolean interactable = true;
    private String permission = null;
    private Predicate<Player> condition = null;
    private long refreshInterval = -1;
    private int slot = -1;

    public SortButtonImpl(String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull SortOrder getCurrentOrder() {
        return currentOrder;
    }

    @Override
    public SortButton order(@NotNull SortOrder order) {
        this.currentOrder = order;
        return this;
    }

    @Override
    public SortButton toggleOrder() {
        this.currentOrder = currentOrder.next();
        return this;
    }

    @Override
    public @NotNull ItemStack getAscendingItem() {
        return ascendingItem != null ? ascendingItem.clone() : new ItemStack(org.bukkit.Material.ARROW);
    }

    @Override
    public SortButton ascendingItem(@NotNull ItemStack item) {
        this.ascendingItem = item.clone();
        return this;
    }

    @Override
    public @NotNull ItemStack getDescendingItem() {
        return descendingItem != null ? descendingItem.clone() : getAscendingItem();
    }

    @Override
    public SortButton descendingItem(@NotNull ItemStack item) {
        this.descendingItem = item.clone();
        return this;
    }

    @Override
    public SortButton onSortChange(@NotNull Consumer<SortChangeEvent> listener) {
        this.sortChangeListener = listener;
        return this;
    }

    @Override
    public <T> void sort(@NotNull List<T> list, @NotNull Comparator<T> comparator) {
        if (currentOrder == SortOrder.DESCENDING) {
            list.sort(Collections.reverseOrder(comparator));
        } else {
            list.sort(comparator);
        }
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return currentOrder == SortOrder.ASCENDING ? getAscendingItem() : getDescendingItem();
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        // 排序按钮不支持直接设置物品，需要通过 ascendingItem/descendingItem 设置
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
    public boolean isInteractable() {
        return interactable;
    }

    @Override
    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    @Override
    public Component onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            SortOrder oldOrder = currentOrder;
            toggleOrder();

            if (sortChangeListener != null) {
                SortChangeEvent changeEvent = new SortChangeEvent(this, oldOrder, currentOrder, player);
                sortChangeListener.accept(changeEvent);
                if (changeEvent.isCancelled()) {
                    currentOrder = oldOrder;
                }
            }
        }

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

    public @Nullable String getPermission() {
        return permission;
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

    public int getRefreshInterval() {
        return (int) refreshInterval;
    }

    @Override
    public Component refreshInterval(int interval) {
        this.refreshInterval = interval;
        return this;
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
    public @NotNull Component clone() {
        SortButtonImpl cloned = new SortButtonImpl(id);
        cloned.currentOrder = this.currentOrder;
        if (this.ascendingItem != null) {
            cloned.ascendingItem = this.ascendingItem.clone();
        }
        if (this.descendingItem != null) {
            cloned.descendingItem = this.descendingItem.clone();
        }
        cloned.movable = this.movable;
        cloned.permission = this.permission;
        cloned.condition = this.condition;
        cloned.refreshInterval = this.refreshInterval;
        cloned.slot = this.slot;
        return cloned;
    }
}
