package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.CloseEvent;
import com.thenextlvl.foliagui.api.event.OpenEvent;
import com.thenextlvl.foliagui.api.sound.GUISound;
import com.thenextlvl.foliagui.api.sound.SoundConfig;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * GUI抽象基类
 * 提供GUI的基本实现，处理事件监听和生命周期
 *
 * @author TheNextLvl
 */
public abstract class AbstractGUI implements GUI, Listener {

    protected final String id;
    protected Inventory inventory;
    protected Player viewer;
    protected boolean isOpen = false;
    protected Location location;
    protected SoundConfig soundConfig;

    private Consumer<ClickEvent> clickHandler;
    private Consumer<OpenEvent> openHandler;
    private Consumer<CloseEvent> closeHandler;

    // 刷新任务
    private Object refreshTask; // BukkitTask or ScheduledTask
    private long lastRefreshTick = 0;

    // 脏检查 - 记录需要更新的槽位
    private final java.util.Set<Integer> dirtySlots = new java.util.HashSet<>();
    private Object batchUpdateTask; // BukkitTask or ScheduledTask
    private static final int BATCH_UPDATE_DELAY = 1; // 1 tick 延迟批量更新

    protected AbstractGUI(@NotNull String id) {
        this.id = id;
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public void open(@NotNull Player player) {
        if (isOpen) {
            close(player);
        }

        this.viewer = player;
        this.location = player.getLocation();

        // 在正确的线程构建和打开
        FoliaScheduler.runOnPlayer(player, () -> {
            buildInventory();
            player.openInventory(inventory);
            isOpen = true;

            // 播放打开音效
            playSound(player, GUISound.Type.OPEN);

            // 注册事件监听
            Bukkit.getPluginManager().registerEvents(this, getPlugin());

            // 启动组件（动画等）
            startComponents();

            // 启动刷新任务
            startRefreshTask();

            // 触发打开事件
            OpenEvent event = new OpenEvent(player, inventory);
            if (openHandler != null) {
                openHandler.accept(event);
            }
            onOpen(player);
        });
    }

    /**
     * 启动所有需要启动的组件（如动画）
     */
    protected void startComponents() {
        if (inventory == null) return;

        java.util.Map<Integer, Component> components = getAllComponents();
        for (java.util.Map.Entry<Integer, Component> entry : components.entrySet()) {
            int slot = entry.getKey();
            Component component = entry.getValue();

            // 处理 AnimatedIcon
            if (component instanceof AnimatedIconImpl animatedIcon) {
                // 绑定玩家以确保正确的线程调度
                animatedIcon.bindPlayer(viewer);
                animatedIcon.setUpdateCallback(() -> {
                    // 动画帧变化时更新槽位
                    updateSlot(slot);
                });
                // 如果标记为播放但还没启动，则启动动画
                if (animatedIcon.isPlaying() && animatedIcon.getTask() == null) {
                    animatedIcon.startAnimation();
                } else {
                    animatedIcon.play();
                }
            }
        }
    }

    /**
     * 停止所有组件（如动画）
     */
    protected void stopComponents() {
        java.util.Map<Integer, Component> components = getAllComponents();
        for (Component component : components.values()) {
            if (component instanceof AnimatedIconImpl animatedIcon) {
                animatedIcon.stop();
                animatedIcon.setUpdateCallback(null);
            }
        }
    }

    @Override
    public void close(@NotNull Player player) {
        if (!isOpen || !player.equals(viewer)) {
            return;
        }

        FoliaScheduler.runOnPlayer(player, () -> {
            player.closeInventory();
            cleanup();
        });
    }

    @Override
    public void refresh() {
        if (!isOpen || viewer == null) return;

        FoliaScheduler.runOnPlayer(viewer, () -> {
            refreshInventory();
            viewer.updateInventory();
        });
    }

    @Override
    public void updateSlot(int slot) {
        if (!isOpen || viewer == null || inventory == null) return;
        if (slot < 0 || slot >= inventory.getSize()) return;

        // 使用脏检查机制：标记槽位为脏，延迟批量更新
        markDirty(slot);
    }

    /**
     * 标记槽位为脏（需要更新）
     * 多次调用会合并，在下一个 tick 批量更新
     *
     * @param slot 槽位
     */
    protected void markDirty(int slot) {
        synchronized (dirtySlots) {
            dirtySlots.add(slot);

            // 如果批量更新任务未启动，启动它
            if (batchUpdateTask == null) {
                startBatchUpdateTask();
            }
        }
    }

    /**
     * 标记多个槽位为脏
     *
     * @param slots 槽位数组
     */
    protected void markDirty(int... slots) {
        synchronized (dirtySlots) {
            for (int slot : slots) {
                dirtySlots.add(slot);
            }

            if (batchUpdateTask == null) {
                startBatchUpdateTask();
            }
        }
    }

    /**
     * 立即更新所有脏槽位
     * 直接在当前线程执行，由批量更新任务调用（已在玩家线程）
     */
    protected void flushDirtySlots() {
        if (!isOpen || viewer == null || inventory == null) return;

        java.util.Set<Integer> slotsToUpdate;
        synchronized (dirtySlots) {
            if (dirtySlots.isEmpty()) return;

            slotsToUpdate = new java.util.HashSet<>(dirtySlots);
            dirtySlots.clear();
        }

        // 直接设置物品（批量更新任务已在玩家线程执行）
        for (int slot : slotsToUpdate) {
            if (slot < 0 || slot >= inventory.getSize()) continue;

            Component component = getComponent(slot);
            if (component != null) {
                inventory.setItem(slot, getDisplayItemResolved(component, viewer));
            }
        }
    }

    /**
     * 启动批量更新任务
     */
    private void startBatchUpdateTask() {
        // 使用 FoliaScheduler 以兼容 Folia
        // runLaterOnPlayer 返回 ScheduledTask（Folia）或 null（非Folia）
        Object task = FoliaScheduler.runLaterOnPlayer(viewer, BATCH_UPDATE_DELAY, () -> {
            flushDirtySlots();
            batchUpdateTask = null;
        });
        batchUpdateTask = task;
    }

    /**
     * 停止批量更新任务
     */
    private void stopBatchUpdateTask() {
        if (batchUpdateTask != null) {
            if (batchUpdateTask instanceof BukkitTask bukkitTask) {
                bukkitTask.cancel();
            } else if (batchUpdateTask instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
                foliaTask.cancel();
            }
            batchUpdateTask = null;
        }
        synchronized (dirtySlots) {
            dirtySlots.clear();
        }
    }

    @Override
    public void dispose() {
        if (viewer != null && isOpen) {
            close(viewer);
        }
        cleanup();
    }

    protected void cleanup() {
        isOpen = false;
        HandlerList.unregisterAll(this);

        // 停止刷新任务
        stopRefreshTask();

        // 停止批量更新任务
        stopBatchUpdateTask();

        // 停止组件（动画等）
        stopComponents();

        if (viewer != null) {
            // 返还 ItemSlot 中的物品和误放入的物品
            returnItemsToPlayer(viewer);

            // 播放关闭音效
            playSound(viewer, GUISound.Type.CLOSE);

            CloseEvent event = new CloseEvent(viewer, inventory);
            if (closeHandler != null) {
                closeHandler.accept(event);
            }
            onClose(viewer);
            viewer = null;
        }
    }

    /**
     * 启动组件刷新任务
     */
    private void startRefreshTask() {
        if (refreshTask != null) {
            return; // 已经在运行
        }

        final long[] openTick = {Bukkit.getCurrentTick()}; // 记录打开时的 tick

        // 使用 FoliaScheduler 以兼容 Folia
        Object task = FoliaScheduler.runTimerOnPlayer(viewer, 1L, 1L, t -> {
            if (!isOpen || viewer == null || inventory == null) {
                stopRefreshTask();
                return;
            }

            long currentTick = Bukkit.getCurrentTick();
            long elapsedTicks = currentTick - openTick[0];
            Map<Integer, Component> components = getAllComponents();

            for (Map.Entry<Integer, Component> entry : components.entrySet()) {
                int slot = entry.getKey();
                Component component = entry.getValue();

                int refreshInterval = component.getRefreshInterval();
                if (refreshInterval > 0 && elapsedTicks % refreshInterval == 0) {
                    // 对于 DynamicIcon，先调用 refresh 更新数据
                    if (component instanceof DynamicIconImpl dynamicIcon && viewer != null) {
                        dynamicIcon.refresh(viewer);
                    }
                    // 刷新这个组件（带占位符解析）
                    inventory.setItem(slot, getDisplayItemResolved(component, viewer));
                }
            }

            lastRefreshTick = currentTick;
        });
        refreshTask = task;
    }

    /**
     * 停止组件刷新任务
     */
    private void stopRefreshTask() {
        if (refreshTask != null) {
            if (refreshTask instanceof BukkitTask bukkitTask) {
                bukkitTask.cancel();
            } else if (refreshTask instanceof io.papermc.paper.threadedregions.scheduler.ScheduledTask foliaTask) {
                foliaTask.cancel();
            }
            refreshTask = null;
        }
    }

    @Override
    public boolean isOpen() {
        return isOpen;
    }

    @Override
    public @Nullable Player getViewer() {
        return viewer;
    }

    @Override
    public @Nullable Location getLocation() {
        return location;
    }

    @Override
    public @Nullable Inventory getInventory() {
        return inventory;
    }

    // ==================== 事件处理 ====================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!isOpen || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.equals(viewer)) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }

        boolean isPlayerInventory = event.getClickedInventory().equals(player.getInventory());
        boolean isGUIInventory = event.getClickedInventory().equals(inventory);
        
        if (isPlayerInventory) {
            handlePlayerInventoryClick(event, player);
            return;
        }

        if (!isGUIInventory) {
            return;
        }

        int slot = event.getSlot();
        
        if (isPaginationSlot(slot)) {
            handlePaginationClick(event, player, slot);
            return;
        }

        Component component = getComponent(slot);
        boolean isComponentMovable = component != null && component.isMovable();
        boolean isItemSlot = component instanceof ItemSlotImpl;

        // ItemSlot 特殊处理：允许物品放入/取出
        if (isItemSlot) {
            handleItemSlotClick(event, player, (ItemSlotImpl) component, slot);
            return;
        }

        if (!isComponentMovable) {
            event.setCancelled(true);
        }

        if (component != null && !component.isInteractable()) {
            return;
        }

        ClickEvent clickEvent = new ClickEvent(
            player,
            event.getClickedInventory(),
            event.getSlot(),
            event.getClick(),
            event.getAction(),
            event.getCurrentItem(),
            event.getCursor()
        );

        if (clickHandler != null) {
            clickHandler.accept(clickEvent);
        }

        if (component != null) {
            handleComponentInteract(clickEvent, component, event.getSlot());
            // 播放点击音效（非ToggleButton组件）
            if (!(component instanceof ToggleButtonImpl)) {
                playSound(player, GUISound.Type.CLICK);
            }
        }

        handleClick(clickEvent);

        if (clickEvent.isCancelled() || !isComponentMovable) {
            event.setCancelled(true);
        }
    }
    
    protected void handleComponentInteract(ClickEvent clickEvent, Component component, int slot) {
        if (component instanceof ToggleButtonImpl toggleButton) {
            boolean oldState = toggleButton.isToggled();
            boolean newState = toggleButton.toggle((Player) clickEvent.getPlayer());
            if (newState != oldState) {
                updateSlot(slot);
                // 播放切换音效
                playSound((Player) clickEvent.getPlayer(), GUISound.Type.TOGGLE);
            }
        } else if (component instanceof SliderImpl slider && slider.isInteractable()) {
            slider.onInteract(new com.thenextlvl.foliagui.api.event.InteractionEvent(
                clickEvent.getPlayer(),
                clickEvent.getInventory(),
                clickEvent.getSlot(),
                clickEvent.getClickType(),
                clickEvent.getAction(),
                clickEvent.getCurrentItem(),
                clickEvent.getCursor()
            ));
            updateSlot(slot);
        } else if (component instanceof ProgressBarImpl progressBar && progressBar.isInteractable()) {
            progressBar.onInteract(new com.thenextlvl.foliagui.api.event.InteractionEvent(
                clickEvent.getPlayer(),
                clickEvent.getInventory(),
                clickEvent.getSlot(),
                clickEvent.getClickType(),
                clickEvent.getAction(),
                clickEvent.getCurrentItem(),
                clickEvent.getCursor()
            ));
            updateSlot(slot);
        } else if (component.isInteractable()) {
            component.onInteract(new com.thenextlvl.foliagui.api.event.InteractionEvent(
                clickEvent.getPlayer(),
                clickEvent.getInventory(),
                clickEvent.getSlot(),
                clickEvent.getClickType(),
                clickEvent.getAction(),
                clickEvent.getCurrentItem(),
                clickEvent.getCursor()
            ));
        }
    }

    /**
     * 处理 ItemSlot 的点击事件
     * <p>
     * ItemSlot 允许玩家放入/取出物品
     */
    private void handleItemSlotClick(InventoryClickEvent event, Player player, ItemSlotImpl itemSlot, int slot) {
        org.bukkit.inventory.ItemStack cursor = event.getCursor();
        org.bukkit.inventory.ItemStack current = event.getCurrentItem();

        // 右键取出物品
        if (event.getClick().isRightClick() && itemSlot.getStoredItem() != null) {
            // 玩家尝试取出物品
            // 立即更新状态（同步）
            itemSlot.setStoredItem(null);
            event.setCancelled(false);
            return;
        }

        // 左键放置物品
        if (event.getClick().isLeftClick() && cursor != null && !cursor.getType().isAir()) {
            if (itemSlot.canAccept(cursor)) {
                // 允许放置
                // 立即更新状态（同步）- 使用cursor的副本作为新物品
                org.bukkit.inventory.ItemStack placedItem = cursor.clone();
                // 如果放置的是单个物品，或者全部放置
                if (cursor.getAmount() == 1 || event.getClick().isLeftClick()) {
                    itemSlot.setStoredItem(placedItem);
                }
                event.setCancelled(false);
                return;
            } else {
                // 不允许放置此物品
                event.setCancelled(true);
                player.sendMessage("§c无法放入此物品");
                return;
            }
        }

        // Shift+点击处理
        if (event.getClick().isShiftClick()) {
            // 从 ItemSlot 取出物品到玩家背包
            if (itemSlot.getStoredItem() != null) {
                // 立即更新状态（同步）
                itemSlot.setStoredItem(null);
                event.setCancelled(false);
                return;
            }
        }

        // 其他情况取消事件
        event.setCancelled(true);
    }

    /**
     * 返还物品给玩家
     * <p>
     * 当 GUI 关闭时，返还所有 ItemSlot 中的物品以及误放入空槽位的物品
     */
    protected void returnItemsToPlayer(@NotNull Player player) {
        java.util.Map<Integer, org.bukkit.inventory.ItemStack> itemsToReturn = new java.util.HashMap<>();

        // 收集所有 ItemSlot 中的物品
        for (java.util.Map.Entry<Integer, Component> entry : getAllComponents().entrySet()) {
            int slot = entry.getKey();
            Component component = entry.getValue();

            if (component instanceof ItemSlotImpl itemSlot) {
                if (itemSlot.isAutoReturn() && itemSlot.getStoredItem() != null) {
                    itemsToReturn.put(slot, itemSlot.getStoredItem());
                    itemSlot.clear();
                }
            }
        }

        // 收集误放入空槽位的物品（没有组件的槽位）
        if (inventory != null) {
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                Component component = getComponent(slot);
                if (component == null) {
                    org.bukkit.inventory.ItemStack item = inventory.getItem(slot);
                    if (item != null && !item.getType().isAir()) {
                        itemsToReturn.put(slot, item);
                        inventory.setItem(slot, null);
                    }
                }
            }
        }

        // 返还物品给玩家
        if (!itemsToReturn.isEmpty()) {
            java.util.Collection<org.bukkit.inventory.ItemStack> leftover = player.getInventory().addItem(
                    itemsToReturn.values().toArray(new org.bukkit.inventory.ItemStack[0])
            ).values();

            // 如果玩家背包满了，掉落物品
            for (org.bukkit.inventory.ItemStack item : leftover) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }

            player.sendMessage("§e已返还 " + itemsToReturn.size() + " 个物品");
        }
    }

    /**
     * 获取所有组件
     */
    protected java.util.Map<Integer, Component> getAllComponents() {
        java.util.Map<Integer, Component> allComponents = new java.util.HashMap<>();
        // 子类需要覆盖此方法返回所有组件
        return allComponents;
    }

    private void handlePlayerInventoryClick(InventoryClickEvent event, Player player) {
        if (!isUsingPlayerInventory()) {
            return;
        }
        
        int slot = event.getSlot();
        Component component = getPlayerInventoryComponent(slot);
        
        if (component == null) {
            return;
        }
        
        event.setCancelled(true);
        
        if (!component.isInteractable()) {
            return;
        }
        
        ClickEvent clickEvent = new ClickEvent(
            player,
            event.getClickedInventory(),
            slot,
            event.getClick(),
            event.getAction(),
            event.getCurrentItem(),
            event.getCursor()
        );
        
        if (clickHandler != null) {
            clickHandler.accept(clickEvent);
        }
        handlePlayerInventoryComponentClick(clickEvent, component);
        
        if (clickEvent.isCancelled()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }
        if (!player.equals(viewer)) {
            return;
        }

        cleanup();
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!isOpen || !(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!player.equals(viewer)) {
            return;
        }
        
        boolean hasMovableComponent = false;
        for (int slot : event.getInventorySlots()) {
            if (slot < inventory.getSize()) {
                Component component = getComponent(slot);
                if (component != null && component.isMovable()) {
                    hasMovableComponent = true;
                    break;
                }
            }
        }
        
        if (!hasMovableComponent) {
            event.setCancelled(true);
        }
    }

    // ==================== 抽象方法 ====================

    /**
     * 构建背包（首次创建）
     */
    protected abstract void buildInventory();

    /**
     * 刷新背包内容（不清除引用，只更新物品）
     */
    protected void refreshInventory() {
        buildInventory();
    }

    /**
     * 获取指定槽位的组件
     */
    protected abstract @Nullable Component getComponent(int slot);

    /**
     * 获取插件实例
     */
    protected abstract @NotNull Plugin getPlugin();

    // ==================== 占位符解析 ====================

    /**
     * 解析物品中的占位符
     * <p>
     * 自动解析物品名称和 Lore 中的 %placeholder% 占位符
     *
     * @param item   原始物品
     * @param player 玩家上下文
     * @return 解析后的物品副本
     */
    @NotNull
    protected ItemStack resolvePlaceholders(@NotNull ItemStack item, @Nullable Player player) {
        if (item == null || item.getType().isAir()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        ItemStack resolved = item.clone();
        ItemMeta resolvedMeta = resolved.getItemMeta();

        // 解析名称
        if (resolvedMeta.hasDisplayName()) {
            String name = resolvedMeta.getDisplayName();
            String resolvedName = com.thenextlvl.foliagui.manager.PlaceholderManager.resolve(player, name);
            resolvedMeta.setDisplayName(resolvedName);
        }

        // 解析 Lore
        if (resolvedMeta.hasLore()) {
            List<String> lore = resolvedMeta.getLore();
            List<String> resolvedLore = new ArrayList<>();
            for (String line : lore) {
                resolvedLore.add(com.thenextlvl.foliagui.manager.PlaceholderManager.resolve(player, line));
            }
            resolvedMeta.setLore(resolvedLore);
        }

        resolved.setItemMeta(resolvedMeta);
        return resolved;
    }

    /**
     * 获取组件的显示物品（带占位符解析）
     *
     * @param component 组件
     * @param player    玩家上下文
     * @return 解析后的显示物品
     */
    @NotNull
    protected ItemStack getDisplayItemResolved(@NotNull Component component, @Nullable Player player) {
        ItemStack item = component.getDisplayItem();
        return resolvePlaceholders(item, player);
    }

    /**
     * 是否使用玩家背包
     */
    protected boolean isUsingPlayerInventory() {
        return false;
    }

    /**
     * 获取玩家背包指定槽位的组件
     */
    protected @Nullable Component getPlayerInventoryComponent(int slot) {
        return null;
    }

    /**
     * 判断槽位是否为分页槽位
     */
    protected boolean isPaginationSlot(int slot) {
        return false;
    }

    /**
     * 处理分页槽位点击
     */
    protected void handlePaginationClick(InventoryClickEvent event, Player player, int slot) {
    }

    /**
     * 处理点击事件（子类可覆盖）
     */
    protected void handleClick(@NotNull ClickEvent event) {
        // 子类覆盖
    }

    /**
     * 处理玩家背包组件点击事件（子类可覆盖）
     */
    protected void handlePlayerInventoryComponentClick(@NotNull ClickEvent event, @NotNull Component component) {
        component.onInteract(new com.thenextlvl.foliagui.api.event.InteractionEvent(
            event.getPlayer(),
            event.getInventory(),
            event.getSlot(),
            event.getClickType(),
            event.getAction(),
            event.getCurrentItem(),
            event.getCursor()
        ));
    }

    /**
     * GUI打开时调用（子类可覆盖）
     */
    public void onOpen(@NotNull Player player) {
        // 子类覆盖
    }

    /**
     * GUI关闭时调用（子类可覆盖）
     */
    public void onClose(@NotNull Player player) {
        // 子类覆盖
    }

    // ==================== 事件处理器设置 ====================

    @Override
    public GUI onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public GUI onOpen(@NotNull Consumer<OpenEvent> handler) {
        this.openHandler = handler;
        return this;
    }

    @Override
    public GUI onClose(@NotNull Consumer<CloseEvent> handler) {
        this.closeHandler = handler;
        return this;
    }

    // ==================== 音效系统 ====================

    /**
     * 获取音效配置
     * @return 音效配置，如果未设置则返回全局默认
     */
    public @NotNull SoundConfig getSoundConfig() {
        return soundConfig != null ? soundConfig : SoundConfig.getGlobalDefault();
    }

    /**
     * 设置音效配置
     * @param config 音效配置
     * @return 此GUI
     */
    public GUI setSoundConfig(@Nullable SoundConfig config) {
        this.soundConfig = config;
        return this;
    }

    /**
     * 播放音效
     * @param player 玩家
     * @param type 音效类型
     */
    protected void playSound(@NotNull Player player, @NotNull GUISound.Type type) {
        SoundConfig config = getSoundConfig();
        if (!config.isEnabled()) {
            return;
        }
        GUISound sound = config.getSound(type);
        if (sound != null) {
            sound.play(player);
        }
    }

    /**
     * 播放点击音效
     * @param player 玩家
     */
    public void playClickSound(@NotNull Player player) {
        playSound(player, GUISound.Type.CLICK);
    }

    /**
     * 播放错误音效
     * @param player 玩家
     */
    public void playErrorSound(@NotNull Player player) {
        playSound(player, GUISound.Type.ERROR);
    }

    /**
     * 播放成功音效
     * @param player 玩家
     */
    public void playSuccessSound(@NotNull Player player) {
        playSound(player, GUISound.Type.SUCCESS);
    }

    /**
     * 播放翻页音效
     * @param player 玩家
     */
    public void playPageChangeSound(@NotNull Player player) {
        playSound(player, GUISound.Type.PAGE_CHANGE);
    }
}
