package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.DynamicIcon;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 动态图标组件实现
 * <p>
 * 根据数据提供器动态生成显示物品
 *
 * @author TheNextLvl
 */
public class DynamicIconImpl implements DynamicIcon {

    private final String id;
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;
    private Consumer<ClickEvent> clickHandler;
    private String permission;
    private Predicate<Player> condition;
    private int refreshInterval = -1;

    // DynamicIcon 特有属性
    private Function<Player, ItemStack> dataProvider;
    private Function<Player, ItemStack> elseProvider;
    private int updateInterval = 20;
    private boolean async = false;
    private ItemStack currentItem;
    private Player boundPlayer;

    public DynamicIconImpl(@NotNull String id) {
        this.id = id;
        this.currentItem = new ItemStack(Material.AIR);
    }

    public DynamicIconImpl(@NotNull String id, @NotNull Function<Player, ItemStack> dataProvider) {
        this.id = id;
        this.dataProvider = dataProvider;
        this.currentItem = new ItemStack(Material.AIR);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        if (currentItem != null && !currentItem.getType().isAir()) {
            return currentItem.clone();
        }
        return ItemBuilder.of(Material.AIR).build();
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        this.currentItem = item.clone();
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
    public @NotNull DynamicIcon clone() {
        DynamicIconImpl clone = new DynamicIconImpl(id, dataProvider);
        clone.slot = this.slot;
        clone.interactable = this.interactable;
        clone.movable = this.movable;
        clone.clickHandler = this.clickHandler;
        clone.permission = this.permission;
        clone.condition = this.condition;
        clone.refreshInterval = this.refreshInterval;
        clone.elseProvider = this.elseProvider;
        clone.updateInterval = this.updateInterval;
        clone.async = this.async;
        clone.currentItem = this.currentItem != null ? this.currentItem.clone() : null;
        return clone;
    }

    @Override
    public DynamicIcon onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public DynamicIcon permission(@Nullable String permission) {
        this.permission = permission;
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return condition;
    }

    @Override
    public DynamicIcon condition(@Nullable Predicate<Player> condition) {
        this.condition = condition;
        return this;
    }

    @Override
    public DynamicIcon refreshInterval(int ticks) {
        this.refreshInterval = ticks;
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return refreshInterval > 0 ? refreshInterval : updateInterval;
    }

    // ==================== DynamicIcon 特有方法 ====================

    @Override
    public @Nullable Function<Player, ItemStack> getDataProvider() {
        return dataProvider;
    }

    @Override
    public @NotNull DynamicIcon dataProvider(@Nullable Function<Player, ItemStack> provider) {
        this.dataProvider = provider;
        return this;
    }

    @Override
    public int getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public @NotNull DynamicIcon updateInterval(int ticks) {
        this.updateInterval = ticks;
        return this;
    }

    @Override
    public boolean isAsync() {
        return async;
    }

    @Override
    public @NotNull DynamicIcon async(boolean async) {
        this.async = async;
        return this;
    }

    @Override
    public @Nullable Function<Player, ItemStack> getElseProvider() {
        return elseProvider;
    }

    @Override
    public @NotNull DynamicIcon elseProvider(@Nullable Function<Player, ItemStack> provider) {
        this.elseProvider = provider;
        return this;
    }

    @Override
    public void refresh(@NotNull Player player) {
        if (dataProvider == null) return;

        try {
            // 检查条件
            if (condition != null && !condition.test(player)) {
                if (elseProvider != null) {
                    currentItem = elseProvider.apply(player);
                }
                return;
            }

            ItemStack newItem = dataProvider.apply(player);
            if (newItem != null) {
                this.currentItem = newItem.clone();
            }
        } catch (Exception e) {
            // 忽略错误，保持当前物品
        }
    }

    @Override
    public void setCurrentItem(@Nullable ItemStack item) {
        this.currentItem = item != null ? item.clone() : null;
    }

    @Override
    public @Nullable ItemStack getCurrentItem() {
        return currentItem;
    }

    /**
     * 绑定玩家
     * <p>
     * 用于刷新时获取正确的玩家上下文
     */
    public void bindPlayer(@NotNull Player player) {
        this.boundPlayer = player;
        // 首次绑定立即刷新
        refresh(player);
    }

    /**
     * 获取绑定的玩家
     */
    @Nullable
    public Player getBoundPlayer() {
        return boundPlayer;
    }

    @Nullable
    public Consumer<ClickEvent> getClickHandler() {
        return clickHandler;
    }

    @Nullable
    public String getPermission() {
        return permission;
    }
}