package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.SearchField;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 搜索框组件实现
 * 
 * @author TheNextLvl
 */
public class SearchFieldImpl implements SearchField {
    
    private final String id;
    private String searchText = "";
    private String placeholder = null;
    private int maxLength = -1;
    private long searchDelay = 300;
    private boolean realtimeSearch = true;
    private Predicate<String> filter;
    private ItemStack normalItem;
    private ItemStack activeItem;
    private ItemStack clearButtonItem;
    private Consumer<SearchChangeEvent> searchChangeListener;
    private Consumer<SearchSubmitEvent> searchSubmitListener;
    private Consumer<ClickEvent> clickHandler;
    private boolean movable = false;
    private boolean interactable = true;
    private String permission = null;
    private Predicate<Player> condition = null;
    private long refreshInterval = -1;
    private int slot = -1;
    
    public SearchFieldImpl(String id) {
        this.id = id;
    }
    
    @Override
    public @NotNull String getId() {
        return id;
    }
    
    @Override
    public @NotNull String getSearchText() {
        return searchText;
    }
    
    @Override
    public SearchField searchText(@NotNull String text) {
        this.searchText = text;
        return this;
    }
    
    @Override
    public @Nullable String getPlaceholder() {
        return placeholder;
    }
    
    @Override
    public SearchField placeholder(@Nullable String placeholder) {
        this.placeholder = placeholder;
        return this;
    }
    
    @Override
    public int getMaxLength() {
        return maxLength;
    }
    
    @Override
    public SearchField maxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }
    
    @Override
    public long getSearchDelay() {
        return searchDelay;
    }
    
    @Override
    public SearchField searchDelay(long delay) {
        this.searchDelay = delay;
        return this;
    }
    
    @Override
    public boolean isRealtimeSearch() {
        return realtimeSearch;
    }
    
    @Override
    public SearchField realtimeSearch(boolean realtime) {
        this.realtimeSearch = realtime;
        return this;
    }
    
    @Override
    public @Nullable Predicate<String> getFilter() {
        return filter;
    }
    
    @Override
    public SearchField filter(@Nullable Predicate<String> filter) {
        this.filter = filter;
        return this;
    }
    
    @Override
    public @NotNull ItemStack getNormalItem() {
        return normalItem != null ? normalItem.clone() : new ItemStack(org.bukkit.Material.PAPER);
    }
    
    @Override
    public SearchField normalItem(@NotNull ItemStack item) {
        this.normalItem = item.clone();
        return this;
    }
    
    @Override
    public @NotNull ItemStack getActiveItem() {
        return activeItem != null ? activeItem.clone() : getNormalItem();
    }
    
    @Override
    public SearchField activeItem(@NotNull ItemStack item) {
        this.activeItem = item.clone();
        return this;
    }
    
    @Override
    public @Nullable ItemStack getClearButtonItem() {
        return clearButtonItem != null ? clearButtonItem.clone() : null;
    }
    
    @Override
    public SearchField clearButtonItem(@Nullable ItemStack item) {
        this.clearButtonItem = item != null ? item.clone() : null;
        return this;
    }
    
    @Override
    public SearchField onSearchChange(@NotNull Consumer<SearchChangeEvent> listener) {
        this.searchChangeListener = listener;
        return this;
    }
    
    @Override
    public SearchField onSearchSubmit(@NotNull Consumer<SearchSubmitEvent> listener) {
        this.searchSubmitListener = listener;
        return this;
    }
    
    @Override
    public void performSearch(@Nullable Player player) {
        if (searchSubmitListener != null) {
            SearchSubmitEvent event = new SearchSubmitEvent(this, searchText, player);
            searchSubmitListener.accept(event);
        }
    }
    
    @Override
    public void clear(@Nullable Player player) {
        String oldText = this.searchText;
        this.searchText = "";
        
        if (searchChangeListener != null) {
            SearchChangeEvent event = new SearchChangeEvent(this, oldText, "", player);
            searchChangeListener.accept(event);
        }
    }
    
    @Override
    public @NotNull ItemStack getDisplayItem() {
        ItemStack item = searchText.isEmpty() ? getNormalItem() : getActiveItem();
        ItemStack display = item.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            if (searchText.isEmpty() && placeholder != null) {
                meta.setDisplayName(placeholder);
            } else {
                meta.setDisplayName("§e搜索: §f" + (searchText.isEmpty() ? "§7(点击输入)" : searchText));
            }
            display.setItemMeta(meta);
        }
        return display;
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        // 搜索框组件不支持直接设置物品
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
            handleInput(player);
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
    
    private void handleInput(@NotNull Player player) {
        player.sendMessage("§e请在聊天栏输入搜索内容...");
        // 这里需要实现聊天监听来接收输入
        // 简化版本：直接打开一个虚拟的输入界面
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
        SearchFieldImpl cloned = new SearchFieldImpl(id);
        cloned.searchText = this.searchText;
        cloned.placeholder = this.placeholder;
        cloned.maxLength = this.maxLength;
        cloned.searchDelay = this.searchDelay;
        cloned.realtimeSearch = this.realtimeSearch;
        cloned.filter = this.filter;
        if (this.normalItem != null) {
            cloned.normalItem = this.normalItem.clone();
        }
        if (this.activeItem != null) {
            cloned.activeItem = this.activeItem.clone();
        }
        if (this.clearButtonItem != null) {
            cloned.clearButtonItem = this.clearButtonItem.clone();
        }
        cloned.movable = this.movable;
        cloned.permission = this.permission;
        cloned.condition = this.condition;
        cloned.refreshInterval = this.refreshInterval;
        cloned.slot = this.slot;
        return cloned;
    }
}
