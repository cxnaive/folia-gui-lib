package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.ScrollableList;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 可滚动列表组件实现
 *
 * @author TheNextLvl
 */
public class ScrollableListImpl implements ScrollableList {

    private final String id;
    private List<ItemStack> items = new ArrayList<>();
    private int scrollOffset = 0;
    private int visibleCount = 9;
    private int[] contentSlots = new int[0];
    private int scrollUpSlot = -1;
    private int scrollDownSlot = -1;
    private int scrollIndicatorSlot = -1;
    private ItemStack scrollUpItem;
    private ItemStack scrollDownItem;
    private ItemStack scrollIndicatorItem;
    private Consumer<ScrollableItemClickEvent> itemClickHandler;
    private Runnable scrollCallback;
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = 0;
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;

    public ScrollableListImpl(@NotNull String id) {
        this.id = id;
        initDefaultItems();
    }

    private void initDefaultItems() {
        scrollUpItem = new ItemStack(Material.ARROW);
        ItemMeta upMeta = scrollUpItem.getItemMeta();
        upMeta.setDisplayName("§e↑ 向上滚动");
        scrollUpItem.setItemMeta(upMeta);

        scrollDownItem = new ItemStack(Material.ARROW);
        ItemMeta downMeta = scrollDownItem.getItemMeta();
        downMeta.setDisplayName("§e↓ 向下滚动");
        scrollDownItem.setItemMeta(downMeta);

        scrollIndicatorItem = new ItemStack(Material.BOOK);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        return new ItemStack(Material.BOOK);
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
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
    public void onInteract(@NotNull InteractionEvent event) {
    }

    @Override
    public @NotNull ScrollableListImpl clone() {
        ScrollableListImpl cloned = new ScrollableListImpl(id);
        cloned.items = new ArrayList<>(items);
        cloned.scrollOffset = scrollOffset;
        cloned.visibleCount = visibleCount;
        cloned.contentSlots = contentSlots.clone();
        cloned.scrollUpSlot = scrollUpSlot;
        cloned.scrollDownSlot = scrollDownSlot;
        cloned.scrollIndicatorSlot = scrollIndicatorSlot;
        cloned.itemClickHandler = itemClickHandler;
        cloned.scrollCallback = scrollCallback;
        cloned.interactable = interactable;
        cloned.permission = permission;
        cloned.condition = condition;
        cloned.refreshInterval = refreshInterval;
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
    public @NotNull ScrollableList onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public @NotNull ScrollableList permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return condition;
    }

    @Override
    public @NotNull ScrollableList condition(@Nullable Predicate<Player> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public @NotNull ScrollableList refreshInterval(int ticks) {
        this.refreshInterval = ticks;
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return refreshInterval;
    }

    // ==================== ScrollableList 方法 ====================

    @Override
    public @NotNull List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public @NotNull ScrollableList setItems(@NotNull List<ItemStack> items) {
        this.items = new ArrayList<>(items);
        if (scrollOffset > Math.max(0, items.size() - visibleCount)) {
            scrollOffset = Math.max(0, items.size() - visibleCount);
        }
        return this;
    }

    @Override
    public @NotNull ScrollableList addItem(@NotNull ItemStack item) {
        items.add(item);
        return this;
    }

    @Override
    public @NotNull ScrollableList clearItems() {
        items.clear();
        scrollOffset = 0;
        return this;
    }

    @Override
    public int getScrollOffset() {
        return scrollOffset;
    }

    @Override
    public @NotNull ScrollableList setScrollOffset(int offset) {
        int maxOffset = Math.max(0, items.size() - visibleCount);
        this.scrollOffset = Math.max(0, Math.min(offset, maxOffset));
        return this;
    }

    @Override
    public boolean scrollUp(int amount) {
        if (!canScrollUp()) return false;
        int oldOffset = scrollOffset;
        scrollOffset = Math.max(0, scrollOffset - amount);
        if (scrollOffset != oldOffset) {
            if (scrollCallback != null) {
                scrollCallback.run();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean scrollDown(int amount) {
        if (!canScrollDown()) return false;
        int oldOffset = scrollOffset;
        int maxOffset = Math.max(0, items.size() - visibleCount);
        scrollOffset = Math.min(maxOffset, scrollOffset + amount);
        if (scrollOffset != oldOffset) {
            if (scrollCallback != null) {
                scrollCallback.run();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean scrollToTop() {
        if (scrollOffset == 0) return false;
        scrollOffset = 0;
        if (scrollCallback != null) {
            scrollCallback.run();
        }
        return true;
    }

    @Override
    public boolean scrollToBottom() {
        int maxOffset = Math.max(0, items.size() - visibleCount);
        if (scrollOffset >= maxOffset) return false;
        scrollOffset = maxOffset;
        if (scrollCallback != null) {
            scrollCallback.run();
        }
        return true;
    }

    @Override
    public boolean canScrollUp() {
        return scrollOffset > 0;
    }

    @Override
    public boolean canScrollDown() {
        return scrollOffset < Math.max(0, items.size() - visibleCount);
    }

    @Override
    public int getVisibleCount() {
        return visibleCount;
    }

    @Override
    public @NotNull ScrollableList setVisibleCount(int count) {
        this.visibleCount = Math.max(1, count);
        return this;
    }

    @Override
    public @NotNull List<ItemStack> getVisibleItems() {
        int start = scrollOffset;
        int end = Math.min(start + visibleCount, items.size());
        if (start >= items.size()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(items.subList(start, end));
    }

    @Override
    public @NotNull int[] getContentSlots() {
        return contentSlots;
    }

    @Override
    public @NotNull ScrollableList setContentSlots(@NotNull int[] slots) {
        this.contentSlots = slots.clone();
        this.visibleCount = slots.length;
        return this;
    }

    @Override
    public int getScrollUpSlot() {
        return scrollUpSlot;
    }

    @Override
    public @NotNull ScrollableList setScrollUpSlot(int slot) {
        this.scrollUpSlot = slot;
        return this;
    }

    @Override
    public int getScrollDownSlot() {
        return scrollDownSlot;
    }

    @Override
    public @NotNull ScrollableList setScrollDownSlot(int slot) {
        this.scrollDownSlot = slot;
        return this;
    }

    @Override
    public @NotNull ScrollableList setScrollButtons(@Nullable ItemStack upItem, @Nullable ItemStack downItem) {
        if (upItem != null) {
            this.scrollUpItem = upItem.clone();
        }
        if (downItem != null) {
            this.scrollDownItem = downItem.clone();
        }
        return this;
    }

    @Override
    public @NotNull ScrollableList onItemClick(@NotNull Consumer<ScrollableItemClickEvent> handler) {
        this.itemClickHandler = handler;
        return this;
    }

    @Override
    public @NotNull ScrollableList onScroll(@NotNull Runnable callback) {
        this.scrollCallback = callback;
        return this;
    }

    @Override
    public @NotNull ItemStack getScrollUpItem() {
        if (!canScrollUp()) {
            return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        return scrollUpItem.clone();
    }

    @Override
    public @NotNull ItemStack getScrollDownItem() {
        if (!canScrollDown()) {
            return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        return scrollDownItem.clone();
    }

    @Override
    public int getScrollIndicatorSlot() {
        return scrollIndicatorSlot;
    }

    @Override
    public @NotNull ScrollableList setScrollIndicatorSlot(int slot) {
        this.scrollIndicatorSlot = slot;
        return this;
    }

    @Override
    public @NotNull ItemStack getScrollIndicatorItem() {
        ItemStack item = scrollIndicatorItem.clone();
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e位置: " + (scrollOffset + 1) + "-" + Math.min(scrollOffset + visibleCount, items.size()) + " / " + items.size());
        item.setItemMeta(meta);
        return item;
    }

    public void setScrollIndicatorItem(@NotNull ItemStack item) {
        this.scrollIndicatorItem = item.clone();
    }

    public Consumer<ScrollableItemClickEvent> getItemClickHandler() {
        return itemClickHandler;
    }

    public Consumer<ClickEvent> getClickHandler() {
        return clickHandler;
    }

    public Runnable getScrollCallback() {
        return scrollCallback;
    }
}
