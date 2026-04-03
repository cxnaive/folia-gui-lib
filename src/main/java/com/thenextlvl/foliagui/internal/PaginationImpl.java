package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Pagination;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 分页组件实现
 *
 * @author TheNextLvl
 */
public class PaginationImpl implements Pagination {

    private final String id;
    private int slot;
    private List<ItemStack> items = new ArrayList<>();
    private int currentPage = 1;
    private int itemsPerPage = 9;
    private int[] contentSlots = new int[0];
    private int previousPageSlot = -1;
    private int nextPageSlot = -1;
    private int pageIndicatorSlot = -1;
    private Consumer<PaginationClickEvent> paginationClickHandler;
    private Consumer<ClickEvent> clickHandler;
    private Runnable pageChangeCallback;
    private ItemStack previousPageItem;
    private ItemStack nextPageItem;
    private ItemStack pageIndicatorItem;
    private boolean interactable = true;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = 0;

    public PaginationImpl(@NotNull String id) {
        this.id = id;
        initDefaultItems();
    }

    private void initDefaultItems() {
        previousPageItem = new ItemStack(Material.ARROW);
        var prevMeta = previousPageItem.getItemMeta();
        prevMeta.setDisplayName("§e上一页");
        previousPageItem.setItemMeta(prevMeta);

        nextPageItem = new ItemStack(Material.ARROW);
        var nextMeta = nextPageItem.getItemMeta();
        nextMeta.setDisplayName("§e下一页");
        nextPageItem.setItemMeta(nextMeta);

        pageIndicatorItem = new ItemStack(Material.BOOK);
    }

    @Override
    public @NotNull String getId() {
        return id;
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
    public @NotNull ItemStack getDisplayItem() {
        return new ItemStack(Material.BOOK);
    }

    @Override
    public void setDisplayItem(@Nullable ItemStack item) {
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
        return false;
    }

    @Override
    public void setMovable(boolean movable) {
    }

    @Override
    public void onInteract(@NotNull InteractionEvent event) {
    }

    @Override
    public PaginationImpl clone() {
        PaginationImpl clone = new PaginationImpl(id);
        clone.items = new ArrayList<>(items);
        clone.currentPage = currentPage;
        clone.itemsPerPage = itemsPerPage;
        clone.contentSlots = contentSlots.clone();
        clone.previousPageSlot = previousPageSlot;
        clone.nextPageSlot = nextPageSlot;
        clone.pageIndicatorSlot = pageIndicatorSlot;
        clone.paginationClickHandler = paginationClickHandler;
        clone.clickHandler = clickHandler;
        clone.pageChangeCallback = pageChangeCallback;
        clone.interactable = interactable;
        clone.permission = permission;
        clone.condition = condition;
        clone.refreshInterval = refreshInterval;
        return clone;
    }

    @Override
    public com.thenextlvl.foliagui.api.component.Component onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public com.thenextlvl.foliagui.api.component.Component permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return condition;
    }

    @Override
    public com.thenextlvl.foliagui.api.component.Component condition(@Nullable Predicate<Player> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public com.thenextlvl.foliagui.api.component.Component refreshInterval(int ticks) {
        this.refreshInterval = ticks;
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return refreshInterval;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getTotalPages() {
        if (items.isEmpty()) return 1;
        return (int) Math.ceil((double) items.size() / itemsPerPage);
    }

    @Override
    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    @Override
    public @NotNull List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override
    public void setItems(@NotNull List<ItemStack> items) {
        this.items = new ArrayList<>(items);
        if (currentPage > getTotalPages()) {
            currentPage = Math.max(1, getTotalPages());
        }
    }

    @Override
    public void addItem(@NotNull ItemStack item) {
        items.add(item);
    }

    @Override
    public void clearItems() {
        items.clear();
        currentPage = 1;
    }

    @Override
    public void goToPage(int page) {
        int totalPages = getTotalPages();
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        this.currentPage = page;
        if (pageChangeCallback != null) {
            pageChangeCallback.run();
        }
    }

    @Override
    public boolean nextPage() {
        if (hasNextPage()) {
            currentPage++;
            if (pageChangeCallback != null) {
                pageChangeCallback.run();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
            if (pageChangeCallback != null) {
                pageChangeCallback.run();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean hasNextPage() {
        return currentPage < getTotalPages();
    }

    @Override
    public boolean hasPreviousPage() {
        return currentPage > 1;
    }

    @Override
    public @NotNull List<ItemStack> getCurrentPageItems() {
        int start = (currentPage - 1) * itemsPerPage;
        int end = Math.min(start + itemsPerPage, items.size());
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
    public void setContentSlots(@NotNull int[] slots) {
        this.contentSlots = slots.clone();
        this.itemsPerPage = slots.length;
    }

    @Override
    public int getPreviousPageSlot() {
        return previousPageSlot;
    }

    @Override
    public void setPreviousPageSlot(int slot) {
        this.previousPageSlot = slot;
    }

    @Override
    public int getNextPageSlot() {
        return nextPageSlot;
    }

    @Override
    public void setNextPageSlot(int slot) {
        this.nextPageSlot = slot;
    }

    @Override
    public int getPageIndicatorSlot() {
        return pageIndicatorSlot;
    }

    @Override
    public void setPageIndicatorSlot(int slot) {
        this.pageIndicatorSlot = slot;
    }

    @Override
    public void onPaginationClick(@NotNull Consumer<PaginationClickEvent> handler) {
        this.paginationClickHandler = handler;
    }

    @Override
    public void onPageChange(@NotNull Runnable callback) {
        this.pageChangeCallback = callback;
    }

    public void handleClick(org.bukkit.entity.Player player, int slot, ItemStack clickedItem) {
        if (paginationClickHandler == null) return;
        
        int[] slots = contentSlots;
        int index = -1;
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slot) {
                index = i;
                break;
            }
        }
        
        if (index == -1) return;
        
        int globalIndex = (currentPage - 1) * itemsPerPage + index;
        if (globalIndex >= items.size()) return;
        
        PaginationClickEvent event = new PaginationClickEvent(player, clickedItem, index, globalIndex);
        paginationClickHandler.accept(event);
    }

    public ItemStack getPreviousPageItem() {
        if (!hasPreviousPage()) {
            return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        return previousPageItem.clone();
    }

    public ItemStack getNextPageItem() {
        if (!hasNextPage()) {
            return new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        }
        return nextPageItem.clone();
    }

    public ItemStack getPageIndicatorItem() {
        ItemStack item = pageIndicatorItem.clone();
        var meta = item.getItemMeta();
        meta.setDisplayName("§e第 " + currentPage + " / " + getTotalPages() + " 页");
        item.setItemMeta(meta);
        return item;
    }

    public void setPreviousPageItem(@NotNull ItemStack item) {
        this.previousPageItem = item.clone();
    }

    public void setNextPageItem(@NotNull ItemStack item) {
        this.nextPageItem = item.clone();
    }

    public void setPageIndicatorItem(@NotNull ItemStack item) {
        this.pageIndicatorItem = item.clone();
    }
}
