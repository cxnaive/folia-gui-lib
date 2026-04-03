package com.thenextlvl.foliagui.builder;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.ItemSlot;
import com.thenextlvl.foliagui.api.component.Pagination;
import com.thenextlvl.foliagui.api.component.ToggleButton;
import com.thenextlvl.foliagui.api.input.ChatInputRequest;
import com.thenextlvl.foliagui.api.sound.GUISound;
import com.thenextlvl.foliagui.api.sound.SoundConfig;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.internal.AbstractGUI;
import com.thenextlvl.foliagui.internal.Button;
import com.thenextlvl.foliagui.internal.Decoration;
import com.thenextlvl.foliagui.internal.ItemSlotImpl;
import com.thenextlvl.foliagui.internal.PaginationImpl;
import com.thenextlvl.foliagui.manager.ChatInputManager;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * GUI构建器 - 使用Builder模式创建GUI
 * 提供流畅的链式API
 *
 * @author TheNextLvl
 */
public class GUIBuilder {

    private final String id;
    private String title = "";
    private int rows = 3;
    private InventoryType inventoryType = InventoryType.CHEST; // 默认为箱子类型
    private boolean usePlayerInventory = false;
    private int[] playerInventorySlots = new int[0];
    private final Map<Integer, Component> components = new HashMap<>();
    private final Map<Integer, Component> playerInventoryComponents = new HashMap<>();
    private Consumer<Player> openHandler;
    private Consumer<Player> closeHandler;
    private Consumer<ClickEvent> clickHandler;
    private Plugin plugin;
    private PaginationImpl pagination;
    private final Map<String, BiConsumer<Player, String>> actionHandlers = new HashMap<>();
    private SoundConfig soundConfig;

    private GUIBuilder(@NotNull String id) {
        this.id = id;
    }

    /**
     * 创建新的GUI构建器
     * @param id GUI唯一标识
     * @return GUIBuilder实例
     */
    public static GUIBuilder create(@NotNull String id) {
        return new GUIBuilder(id);
    }

    /**
     * 设置GUI标题
     * @param title 标题（支持颜色代码&）
     * @return 此构建器
     */
    public GUIBuilder title(@NotNull String title) {
        this.title = title.replace('&', '§');
        return this;
    }

    /**
     * 设置GUI行数（1-6）
     * 注意：仅适用于 CHEST 类型
     * @param rows 行数
     * @return 此构建器
     */
    public GUIBuilder rows(int rows) {
        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("Rows must be between 1 and 6");
        }
        this.rows = rows;
        return this;
    }

    /**
     * 设置库存类型
     * <p>
     * 支持的类型：
     * - CHEST: 箱子（可设置行数1-6，最常用）
     * - HOPPER: 漏斗（5槽位，适合小型菜单）
     * - DISPENSER: 发射器（9槽位，3x3布局）
     * - DROPPER: 投掷器（9槽位，3x3布局）
     *
     * @param type 库存类型
     * @return 此构建器
     */
    public GUIBuilder type(@NotNull InventoryType type) {
        // 验证类型是否支持作为菜单
        if (!isMenuSupported(type)) {
            throw new IllegalArgumentException("不支持此库存类型作为菜单: " + type +
                "\n支持的类型: CHEST, HOPPER, DISPENSER, DROPPER");
        }
        this.inventoryType = type;
        // 根据类型自动设置槽位数量
        switch (type) {
            case CHEST -> {} // 保持 rows 设置
            case HOPPER -> this.rows = 1; // 5槽位
            case DISPENSER, DROPPER -> this.rows = 1; // 9槽位
            default -> this.rows = 1;
        }
        return this;
    }

    /**
     * 检查库存类型是否支持作为菜单
     */
    private static boolean isMenuSupported(@NotNull InventoryType type) {
        return type == InventoryType.CHEST
            || type == InventoryType.HOPPER
            || type == InventoryType.DISPENSER
            || type == InventoryType.DROPPER;
    }

    /**
     * 获取库存类型
     * @return 库存类型
     */
    public InventoryType getType() {
        return inventoryType;
    }

    /**
     * 启用玩家背包作为菜单的一部分
     * 当启用时，点击玩家背包中指定槽位也会触发组件事件
     * @param slots 使用的背包槽位（0-35，0-8为快捷栏，9-35为背包）
     * @return 此构建器
     */
    public GUIBuilder usePlayerInventory(int... slots) {
        this.usePlayerInventory = true;
        this.playerInventorySlots = slots != null ? slots : new int[0];
        return this;
    }

    /**
     * 在玩家背包指定槽位添加按钮
     * 注意：需要先调用 usePlayerInventory() 启用背包菜单功能
     * @param playerSlot 玩家背包槽位（0-35）
     * @param item 显示物品
     * @param handler 点击处理器
     * @return 此构建器
     */
    public GUIBuilder playerInventoryButton(int playerSlot, @NotNull ItemStack item, @Nullable Consumer<ClickEvent> handler) {
        if (playerSlot < 0 || playerSlot > 35) {
            throw new IllegalArgumentException("Player inventory slot must be between 0 and 35");
        }
        Button button = new Button(UUID.randomUUID().toString(), item);
        if (handler != null) {
            button.onClick(handler);
        }
        this.playerInventoryComponents.put(playerSlot, button);
        this.usePlayerInventory = true;
        return this;
    }

    /**
     * 在玩家背包指定槽位添加按钮（无处理器）
     * @param playerSlot 玩家背包槽位（0-35）
     * @param item 显示物品
     * @return 此构建器
     */
    public GUIBuilder playerInventoryButton(int playerSlot, @NotNull ItemStack item) {
        return playerInventoryButton(playerSlot, item, null);
    }

    /**
     * 添加组件到指定槽位
     * @param slot 槽位
     * @param component 组件
     * @return 此构建器
     */
    public GUIBuilder component(int slot, @NotNull Component component) {
        this.components.put(slot, component);
        return this;
    }

    /**
     * 添加按钮（简化方法）
     * @param slot 槽位
     * @param item 显示物品
     * @param handler 点击处理器
     * @return 此构建器
     */
    public GUIBuilder button(int slot, @NotNull ItemStack item, @Nullable Consumer<ClickEvent> handler) {
        Button button = new Button(UUID.randomUUID().toString(), item);
        if (handler != null) {
            button.onClick(handler);
        }
        return component(slot, button);
    }

    /**
     * 添加按钮（无处理器）
     * @param slot 槽位
     * @param item 显示物品
     * @return 此构建器
     */
    public GUIBuilder button(int slot, @NotNull ItemStack item) {
        return button(slot, item, null);
    }

    /**
     * 添加动态按钮（每次刷新时重新生成物品）
     * <p>
     * 默认每 20 ticks (1秒) 自动刷新一次
     *
     * @param slot 槽位
     * @param itemSupplier 物品供应器
     * @param handler 点击处理器
     * @return 此构建器
     */
    public GUIBuilder dynamicButton(int slot, @NotNull Supplier<ItemStack> itemSupplier, @Nullable Consumer<ClickEvent> handler) {
        com.thenextlvl.foliagui.internal.DynamicButton button =
            new com.thenextlvl.foliagui.internal.DynamicButton(UUID.randomUUID().toString(), itemSupplier);
        if (handler != null) {
            button.onClick(handler);
        }
        // 默认每秒刷新一次
        button.refreshInterval(20);
        return component(slot, button);
    }

    /**
     * 添加动态按钮（无处理器）
     * <p>
     * 默认每 20 ticks (1秒) 自动刷新一次
     *
     * @param slot 槽位
     * @param itemSupplier 物品供应器
     * @return 此构建器
     */
    public GUIBuilder dynamicButton(int slot, @NotNull Supplier<ItemStack> itemSupplier) {
        return dynamicButton(slot, itemSupplier, null);
    }

    /**
     * 添加开关按钮
     * @param slot 槽位
     * @param onItem 开启状态物品
     * @param offItem 关闭状态物品
     * @param initialState 初始状态
     * @param handler 状态切换处理器
     * @return 此构建器
     */
    public GUIBuilder toggleButton(int slot, @NotNull ItemStack onItem, @NotNull ItemStack offItem, 
                                    boolean initialState, @Nullable Consumer<ToggleButton.ToggleEvent> handler) {
        com.thenextlvl.foliagui.internal.ToggleButtonImpl toggleButton = 
            new com.thenextlvl.foliagui.internal.ToggleButtonImpl(UUID.randomUUID().toString(), onItem, offItem);
        toggleButton.setToggled(initialState);
        if (handler != null) {
            toggleButton.onToggle(handler);
        }
        return component(slot, toggleButton);
    }

    /**
     * 添加开关按钮（默认关闭状态）
     * @param slot 槽位
     * @param onItem 开启状态物品
     * @param offItem 关闭状态物品
     * @param handler 状态切换处理器
     * @return 此构建器
     */
    public GUIBuilder toggleButton(int slot, @NotNull ItemStack onItem, @NotNull ItemStack offItem, 
                                    @Nullable Consumer<ToggleButton.ToggleEvent> handler) {
        return toggleButton(slot, onItem, offItem, false, handler);
    }

    /**
     * 添加开关按钮（无处理器）
     * @param slot 槽位
     * @param onItem 开启状态物品
     * @param offItem 关闭状态物品
     * @return 此构建器
     */
    public GUIBuilder toggleButton(int slot, @NotNull ItemStack onItem, @NotNull ItemStack offItem) {
        return toggleButton(slot, onItem, offItem, false, null);
    }

    /**
     * 添加输入按钮 - 点击后请求玩家在聊天栏输入
     * <p>
     * 点击后会：
     * 1. 关闭当前GUI
     * 2. 发送提示消息
     * 3. 等待玩家输入
     * 4. 输入完成后恢复GUI
     *
     * @param slot 槽位
     * @param item 显示物品
     * @param prompt 输入提示消息
     * @param inputCallback 输入完成回调
     * @return 此构建器
     */
    public GUIBuilder inputButton(int slot, @NotNull ItemStack item, @NotNull String prompt,
                                   @NotNull Consumer<ChatInputRequest.InputResult> inputCallback) {
        return button(slot, item, event -> {
            Player player = event.getPlayer();
            ChatInputManager inputManager = ChatInputManager.getInstance();
            if (inputManager != null) {
                inputManager.requestInput(player)
                        .prompt(prompt)
                        .restoreGUI(true)
                        .onComplete(inputCallback)
                        .submit();
            }
            event.setCancelled(true);
        });
    }

    /**
     * 添加输入按钮（自定义配置）
     * <p>
     * 点击后会：
     * 1. 关闭当前GUI
     * 2. 发送提示消息
     * 3. 等待玩家输入
     * 4. 输入完成后恢复GUI
     *
     * @param slot 槽位
     * @param item 显示物品
     * @param config 输入配置器
     * @return 此构建器
     */
    public GUIBuilder inputButton(int slot, @NotNull ItemStack item,
                                   @NotNull Consumer<ChatInputRequest.Builder> config) {
        return button(slot, item, event -> {
            Player player = event.getPlayer();
            ChatInputManager inputManager = ChatInputManager.getInstance();
            if (inputManager != null) {
                ChatInputRequest.Builder builder = inputManager.requestInput(player);
                config.accept(builder);
                builder.submit();
            }
            event.setCancelled(true);
        });
    }

    /**
     * 添加输入按钮（指定恢复的GUI）
     *
     * @param slot 槽位
     * @param item 显示物品
     * @param prompt 输入提示消息
     * @param restoreGuiId 输入完成后要打开的GUI ID
     * @param inputCallback 输入完成回调
     * @return 此构建器
     */
    public GUIBuilder inputButton(int slot, @NotNull ItemStack item, @NotNull String prompt,
                                   @NotNull String restoreGuiId,
                                   @NotNull Consumer<ChatInputRequest.InputResult> inputCallback) {
        return button(slot, item, event -> {
            Player player = event.getPlayer();
            ChatInputManager inputManager = ChatInputManager.getInstance();
            if (inputManager != null) {
                inputManager.requestInputAndRestore(player, prompt, restoreGuiId, inputCallback);
            }
            event.setCancelled(true);
        });
    }

    /**
     * 添加进度条组件（单图标，在 Lore 中显示文本进度条）
     * @param slot 槽位
     * @return 进度条组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.ProgressBar progressBar(int slot) {
        com.thenextlvl.foliagui.internal.ProgressBarImpl progressBar =
            new com.thenextlvl.foliagui.internal.ProgressBarImpl(UUID.randomUUID().toString());
        component(slot, progressBar);
        return progressBar;
    }

    /**
     * 添加滑块组件（单图标，在 Lore 中显示滑块轨道）
     * @param slot 槽位
     * @return 滑块组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.Slider slider(int slot) {
        com.thenextlvl.foliagui.internal.SliderImpl slider =
            new com.thenextlvl.foliagui.internal.SliderImpl(UUID.randomUUID().toString());
        component(slot, slider);
        return slider;
    }

    /**
     * 添加输入框组件
     * @param slot 槽位
     * @return 输入框组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.InputField inputField(int slot) {
        com.thenextlvl.foliagui.internal.InputFieldImpl inputField = 
            new com.thenextlvl.foliagui.internal.InputFieldImpl(UUID.randomUUID().toString());
        component(slot, inputField);
        return inputField;
    }

    /**
     * 添加搜索框组件
     * @param slot 槽位
     * @return 搜索框组件
     */
    public com.thenextlvl.foliagui.api.component.SearchField searchField(int slot) {
        com.thenextlvl.foliagui.internal.SearchFieldImpl searchField = 
            new com.thenextlvl.foliagui.internal.SearchFieldImpl(UUID.randomUUID().toString());
        component(slot, searchField);
        return searchField;
    }

    /**
     * 添加排序按钮组件
     * @param slot 槽位
     * @return 排序按钮组件
     */
    public com.thenextlvl.foliagui.api.component.SortButton sortButton(int slot) {
        com.thenextlvl.foliagui.internal.SortButtonImpl sortButton =
            new com.thenextlvl.foliagui.internal.SortButtonImpl(UUID.randomUUID().toString());
        component(slot, sortButton);
        return sortButton;
    }

    /**
     * 添加可滚动列表组件
     * @param contentSlots 内容槽位数组
     * @return 可滚动列表组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.ScrollableList scrollableList(int... contentSlots) {
        com.thenextlvl.foliagui.internal.ScrollableListImpl scrollableList =
            new com.thenextlvl.foliagui.internal.ScrollableListImpl(UUID.randomUUID().toString());
        if (contentSlots != null && contentSlots.length > 0) {
            scrollableList.setContentSlots(contentSlots);
        }
        // 注册滚动按钮槽位的占位组件
        // 注意：ScrollableList 需要配合 GUI 的 render 逻辑使用
        return scrollableList;
    }

    /**
     * 添加动画图标组件
     * @param slot 槽位
     * @return 动画图标组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.AnimatedIcon animatedIcon(int slot) {
        com.thenextlvl.foliagui.internal.AnimatedIconImpl animatedIcon =
            new com.thenextlvl.foliagui.internal.AnimatedIconImpl(UUID.randomUUID().toString());
        component(slot, animatedIcon);
        return animatedIcon;
    }

    /**
     * 添加动态图标组件
     * <p>
     * 动态图标根据数据提供器生成显示物品，支持定时刷新
     * @param slot 槽位
     * @return 动态图标组件，可用于进一步配置
     */
    public com.thenextlvl.foliagui.api.component.DynamicIcon dynamicIcon(int slot) {
        com.thenextlvl.foliagui.internal.DynamicIconImpl dynamicIcon =
            new com.thenextlvl.foliagui.internal.DynamicIconImpl(UUID.randomUUID().toString());
        component(slot, dynamicIcon);
        return dynamicIcon;
    }

    /**
     * 添加动态图标组件（带数据提供器）
     * <p>
     * 动态图标根据数据提供器生成显示物品，支持定时刷新
     * @param slot     槽位
     * @param provider 数据提供器
     * @return 动态图标组件
     */
    public com.thenextlvl.foliagui.api.component.DynamicIcon dynamicIcon(int slot,
            @NotNull Function<Player, ItemStack> provider) {
        com.thenextlvl.foliagui.internal.DynamicIconImpl dynamicIcon =
            new com.thenextlvl.foliagui.internal.DynamicIconImpl(UUID.randomUUID().toString());
        dynamicIcon.dataProvider(provider);
        component(slot, dynamicIcon);
        return dynamicIcon;
    }

    /**
     * 添加装饰性物品（不可交互）
     * @param slot 槽位
     * @param item 物品
     * @return 此构建器
     */
    public GUIBuilder decoration(int slot, @NotNull ItemStack item) {
        Component decoration = new Decoration(UUID.randomUUID().toString(), item);
        return component(slot, decoration);
    }

    /**
     * 批量填充相同物品
     * @param item 物品
     * @param slots 槽位数组
     * @return 此构建器
     */
    public GUIBuilder fill(@NotNull ItemStack item, int... slots) {
        for (int slot : slots) {
            decoration(slot, item);
        }
        return this;
    }

    /**
     * 填充单个槽位
     * @param slot 槽位
     * @param item 物品
     * @return 此构建器
     */
    public GUIBuilder fillSlot(int slot, @NotNull ItemStack item) {
        return decoration(slot, item);
    }

    /**
     * 添加动作处理器
     * @param name 处理器名称
     * @param handler 处理器
     * @return 此构建器
     */
    public GUIBuilder actionHandler(@NotNull String name, @NotNull BiConsumer<Player, String> handler) {
        this.actionHandlers.put(name.toLowerCase(), handler);
        return this;
    }

    /**
     * 添加可移动按钮（玩家可以取出物品）
     * @param slot 槽位
     * @param item 显示物品
     * @param handler 点击处理器
     * @return 此构建器
     */
    public GUIBuilder movableButton(int slot, @NotNull ItemStack item, @Nullable Consumer<ClickEvent> handler) {
        Button button = new Button(UUID.randomUUID().toString(), item);
        button.setMovable(true);
        if (handler != null) {
            button.onClick(handler);
        }
        return component(slot, button);
    }

    /**
     * 添加可移动按钮（无处理器）
     * @param slot 槽位
     * @param item 显示物品
     * @return 此构建器
     */
    public GUIBuilder movableButton(int slot, @NotNull ItemStack item) {
        return movableButton(slot, item, null);
    }

    /**
     * 批量填充范围
     * @param item 物品
     * @param start 起始槽位
     * @param end 结束槽位（包含）
     * @return 此构建器
     */
    public GUIBuilder fillRange(@NotNull ItemStack item, int start, int end) {
        for (int i = start; i <= end; i++) {
            decoration(i, item);
        }
        return this;
    }

    /**
     * 使用材质填充边框
     * @param material 材质
     * @return 此构建器
     */
    public GUIBuilder border(@NotNull Material material) {
        int size = rows * 9;
        ItemStack item = new ItemStack(material);

        // 顶部和底部
        for (int i = 0; i < 9; i++) {
            decoration(i, item);
            decoration(size - 9 + i, item);
        }
        // 两侧
        for (int i = 1; i < rows - 1; i++) {
            decoration(i * 9, item);
            decoration(i * 9 + 8, item);
        }
        return this;
    }

    /**
     * 添加分页组件
     * @param contentSlots 内容槽位数组
     * @param items 分页物品列表
     * @param handler 点击处理器
     * @return 此构建器
     */
    public GUIBuilder pagination(@NotNull int[] contentSlots, @NotNull List<ItemStack> items, 
                                  @Nullable Consumer<Pagination.PaginationClickEvent> handler) {
        this.pagination = new PaginationImpl("pagination");
        this.pagination.setContentSlots(contentSlots);
        this.pagination.setItems(items);
        if (handler != null) {
            this.pagination.onPaginationClick(handler);
        }
        return this;
    }

    /**
     * 添加分页组件（无处理器）
     * @param contentSlots 内容槽位数组
     * @param items 分页物品列表
     * @return 此构建器
     */
    public GUIBuilder pagination(@NotNull int[] contentSlots, @NotNull List<ItemStack> items) {
        return pagination(contentSlots, items, null);
    }

    /**
     * 设置分页导航按钮位置
     * @param prevSlot 上一页按钮槽位
     * @param nextSlot 下一页按钮槽位
     * @return 此构建器
     */
    public GUIBuilder paginationNav(int prevSlot, int nextSlot) {
        if (this.pagination == null) {
            this.pagination = new PaginationImpl("pagination");
        }
        this.pagination.setPreviousPageSlot(prevSlot);
        this.pagination.setNextPageSlot(nextSlot);
        return this;
    }

    /**
     * 设置分页导航按钮位置和物品
     * @param prevSlot 上一页按钮槽位
     * @param prevItem 上一页按钮物品
     * @param nextSlot 下一页按钮槽位
     * @param nextItem 下一页按钮物品
     * @return 此构建器
     */
    public GUIBuilder paginationNav(int prevSlot, @NotNull ItemStack prevItem, 
                                     int nextSlot, @NotNull ItemStack nextItem) {
        if (this.pagination == null) {
            this.pagination = new PaginationImpl("pagination");
        }
        this.pagination.setPreviousPageSlot(prevSlot);
        this.pagination.setPreviousPageItem(prevItem);
        this.pagination.setNextPageSlot(nextSlot);
        this.pagination.setNextPageItem(nextItem);
        return this;
    }

    /**
     * 设置页码显示位置
     * @param slot 槽位，-1表示不显示
     * @return 此构建器
     */
    public GUIBuilder paginationIndicator(int slot) {
        if (this.pagination == null) {
            this.pagination = new PaginationImpl("pagination");
        }
        this.pagination.setPageIndicatorSlot(slot);
        return this;
    }

    /**
     * 设置页码显示位置和物品
     * @param slot 槽位
     * @param item 显示物品
     * @return 此构建器
     */
    public GUIBuilder paginationIndicator(int slot, @NotNull ItemStack item) {
        if (this.pagination == null) {
            this.pagination = new PaginationImpl("pagination");
        }
        this.pagination.setPageIndicatorSlot(slot);
        this.pagination.setPageIndicatorItem(item);
        return this;
    }

    /**
     * 设置页面切换回调
     * @param callback 回调
     * @return 此构建器
     */
    public GUIBuilder onPageChange(@NotNull Runnable callback) {
        if (this.pagination == null) {
            this.pagination = new PaginationImpl("pagination");
        }
        this.pagination.onPageChange(callback);
        return this;
    }

    /**
     * 设置打开事件处理器
     * @param handler 处理器
     * @return 此构建器
     */
    public GUIBuilder onOpen(@NotNull Consumer<Player> handler) {
        this.openHandler = handler;
        return this;
    }

    /**
     * 设置关闭事件处理器
     * @param handler 处理器
     * @return 此构建器
     */
    public GUIBuilder onClose(@NotNull Consumer<Player> handler) {
        this.closeHandler = handler;
        return this;
    }

    /**
     * 设置点击事件处理器
     * @param handler 处理器
     * @return 此构建器
     */
    public GUIBuilder onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    /**
     * 设置插件实例
     * @param plugin 插件
     * @return 此构建器
     */
    public GUIBuilder plugin(@NotNull Plugin plugin) {
        this.plugin = plugin;
        return this;
    }

    /**
     * 设置音效配置
     * @param config 音效配置
     * @return 此构建器
     */
    public GUIBuilder soundConfig(@NotNull SoundConfig config) {
        this.soundConfig = config;
        return this;
    }

    /**
     * 设置单个音效
     * @param type 音效类型
     * @param sound 音效
     * @return 此构建器
     */
    public GUIBuilder sound(@NotNull GUISound.Type type, @NotNull GUISound sound) {
        if (this.soundConfig == null) {
            this.soundConfig = SoundConfig.create();
        }
        this.soundConfig.setSound(type, sound);
        return this;
    }

    /**
     * 禁用音效
     * @return 此构建器
     */
    public GUIBuilder silent() {
        this.soundConfig = SoundConfig.silent();
        return this;
    }

    /**
     * 添加物品填充格组件
     * <p>
     * 物品填充格是一个可以接收玩家放入物品的槽位：
     * - 界面关闭时自动返还物品
     * - 玩家掉线时自动返还物品
     * - 可设置占位符物品
     *
     * @param slot 槽位
     * @return ItemSlot 组件实例
     */
    public ItemSlot itemSlot(int slot) {
        ItemSlotImpl itemSlot = new ItemSlotImpl(UUID.randomUUID().toString());
        components.put(slot, itemSlot);
        return itemSlot;
    }

    /**
     * 添加物品填充格组件（带占位符）
     *
     * @param slot 槽位
     * @param placeholder 占位符物品（当槽位为空时显示）
     * @return ItemSlot 组件实例
     */
    public ItemSlot itemSlot(int slot, @Nullable ItemStack placeholder) {
        ItemSlotImpl itemSlot = new ItemSlotImpl(UUID.randomUUID().toString());
        itemSlot.placeholder(placeholder);
        components.put(slot, itemSlot);
        return itemSlot;
    }

    /**
     * 添加空白物品填充格
     * <p>
     * 完全空白的槽位，可接收物品，关闭时自动返还
     *
     * @param slot 槽位
     * @return ItemSlot 组件实例
     */
    public ItemSlot emptySlot(int slot) {
        ItemSlotImpl itemSlot = new ItemSlotImpl(UUID.randomUUID().toString());
        itemSlot.placeholder(null); // 无占位符，完全空白
        components.put(slot, itemSlot);
        return itemSlot;
    }

    /**
     * 构建GUI
     * @return GUI实例
     */
    public GUI build() {
        return new BuiltGUI(this);
    }

    /**
     * 构建的GUI实现
     */
    private class BuiltGUI extends AbstractGUI {
        private final String title;
        private final int rows;
        private final InventoryType inventoryType;
        private final Map<Integer, Component> components;
        private final boolean usePlayerInventory;
        private final int[] playerInventorySlots;
        private final Map<Integer, Component> playerInventoryComponents;
        private final Consumer<Player> openHandler;
        private final Consumer<Player> closeHandler;
        private final Consumer<ClickEvent> clickHandler;
        private final Plugin plugin;
        private final PaginationImpl pagination;
        
        private final Map<Integer, ItemStack> savedPlayerItems = new HashMap<>();

        BuiltGUI(GUIBuilder builder) {
            super(builder.id);
            this.title = builder.title;
            this.rows = builder.rows;
            this.inventoryType = builder.inventoryType;
            this.components = new HashMap<>(builder.components);
            this.usePlayerInventory = builder.usePlayerInventory;
            this.playerInventorySlots = builder.playerInventorySlots;
            this.playerInventoryComponents = new HashMap<>(builder.playerInventoryComponents);
            this.openHandler = builder.openHandler;
            this.closeHandler = builder.closeHandler;
            this.clickHandler = builder.clickHandler;
            this.plugin = builder.plugin;
            this.pagination = builder.pagination;

            // 设置音效配置
            if (builder.soundConfig != null) {
                this.soundConfig = builder.soundConfig;
            }

            if (this.pagination != null) {
                this.pagination.onPageChange(this::refresh);
            }
        }

        @Override
        protected void buildInventory() {
            if (this.inventory == null) {
                // 根据库存类型创建不同大小的库存
                if (inventoryType == InventoryType.CHEST) {
                    this.inventory = Bukkit.createInventory(null, rows * 9, title);
                } else {
                    // 对于其他类型，使用类型默认大小
                    this.inventory = Bukkit.createInventory(null, inventoryType, title);
                }
            }
            inventory.clear();
            components.forEach((slot, component) -> {
                component.setSlot(slot);
                // 对于 DynamicIcon，先调用 refresh 更新数据
                if (component instanceof com.thenextlvl.foliagui.internal.DynamicIconImpl dynamicIcon && viewer != null) {
                    dynamicIcon.refresh(viewer);
                }
                // 使用带占位符解析的方法
                inventory.setItem(slot, getDisplayItemResolved(component, viewer));
            });

            if (pagination != null) {
                buildPagination();
            }
        }

        @Override
        protected void refreshInventory() {
            if (inventory == null) return;
            inventory.clear();
            components.forEach((slot, component) -> {
                // 对于 DynamicIcon，先调用 refresh 更新数据
                if (component instanceof com.thenextlvl.foliagui.internal.DynamicIconImpl dynamicIcon && viewer != null) {
                    dynamicIcon.refresh(viewer);
                }
                // 使用带占位符解析的方法
                inventory.setItem(slot, getDisplayItemResolved(component, viewer));
            });

            if (pagination != null) {
                buildPagination();
            }
        }
        
        private void buildPagination() {
            List<ItemStack> pageItems = pagination.getCurrentPageItems();
            int[] slots = pagination.getContentSlots();
            
            for (int i = 0; i < slots.length && i < pageItems.size(); i++) {
                inventory.setItem(slots[i], pageItems.get(i));
            }
            
            if (pagination.getPreviousPageSlot() >= 0) {
                inventory.setItem(pagination.getPreviousPageSlot(), pagination.getPreviousPageItem());
            }
            if (pagination.getNextPageSlot() >= 0) {
                inventory.setItem(pagination.getNextPageSlot(), pagination.getNextPageItem());
            }
            if (pagination.getPageIndicatorSlot() >= 0) {
                inventory.setItem(pagination.getPageIndicatorSlot(), pagination.getPageIndicatorItem());
            }
        }

        @Override
        protected Component getComponent(int slot) {
            return components.get(slot);
        }

        @Override
        protected java.util.Map<Integer, Component> getAllComponents() {
            return new java.util.HashMap<>(components);
        }

        @Override
        protected boolean isPaginationSlot(int slot) {
            if (pagination == null) return false;
            
            for (int s : pagination.getContentSlots()) {
                if (s == slot) return true;
            }
            if (slot == pagination.getPreviousPageSlot()) return true;
            if (slot == pagination.getNextPageSlot()) return true;
            if (slot == pagination.getPageIndicatorSlot()) return true;
            
            return false;
        }

        @Override
        protected @NotNull Plugin getPlugin() {
            if (plugin != null) {
                return plugin;
            }
            com.thenextlvl.foliagui.manager.GUIManager manager = com.thenextlvl.foliagui.manager.GUIManager.getInstance();
            if (manager != null) {
                return manager.getPlugin();
            }
            throw new IllegalStateException("Plugin not set. Call .plugin() before build()");
        }

        @Override
        public void open(@NotNull Player player) {
            getPlugin();
            if (usePlayerInventory) {
                setupPlayerInventory(player);
            }
            super.open(player);
        }

        private void setupPlayerInventory(@NotNull Player player) {
            savedPlayerItems.clear();
            
            Set<Integer> allSlots = new HashSet<>();
            for (int slot : playerInventorySlots) {
                allSlots.add(slot);
            }
            allSlots.addAll(playerInventoryComponents.keySet());
            
            for (int slot : allSlots) {
                ItemStack existing = player.getInventory().getItem(slot);
                if (existing != null && !existing.getType().isAir()) {
                    savedPlayerItems.put(slot, existing.clone());
                }
            }
            
            playerInventoryComponents.forEach((slot, component) -> {
                component.setSlot(slot);
                player.getInventory().setItem(slot, component.getDisplayItem());
            });
        }

        private void restorePlayerInventory(@NotNull Player player) {
            Set<Integer> allSlots = new HashSet<>();
            for (int slot : playerInventorySlots) {
                allSlots.add(slot);
            }
            allSlots.addAll(playerInventoryComponents.keySet());
            
            for (int slot : allSlots) {
                if (savedPlayerItems.containsKey(slot)) {
                    player.getInventory().setItem(slot, savedPlayerItems.get(slot));
                } else {
                    player.getInventory().setItem(slot, null);
                }
            }
            savedPlayerItems.clear();
        }

        @Override
        public void close(@NotNull Player player) {
            super.close(player);
        }

        @Override
        public void onOpen(Player player) {
            if (openHandler != null) {
                openHandler.accept(player);
            }
        }

        @Override
        public void onClose(Player player) {
            if (usePlayerInventory) {
                restorePlayerInventory(player);
            }
            if (closeHandler != null) {
                closeHandler.accept(player);
            }
        }

        @Override
        protected void handleClick(ClickEvent event) {
            if (clickHandler != null) {
                clickHandler.accept(event);
            }
        }

        @Override
        protected boolean isUsingPlayerInventory() {
            return usePlayerInventory;
        }

        @Override
        protected Component getPlayerInventoryComponent(int slot) {
            return playerInventoryComponents.get(slot);
        }
        
        @Override
        protected void handlePaginationClick(org.bukkit.event.inventory.InventoryClickEvent event, Player player, int slot) {
            event.setCancelled(true);

            if (pagination == null) return;

            if (slot == pagination.getPreviousPageSlot()) {
                if (pagination.previousPage()) {
                    player.sendMessage("§7已切换到第 " + pagination.getCurrentPage() + " 页");
                    playPageChangeSound(player);
                }
                return;
            }

            if (slot == pagination.getNextPageSlot()) {
                if (pagination.nextPage()) {
                    player.sendMessage("§7已切换到第 " + pagination.getCurrentPage() + " 页");
                    playPageChangeSound(player);
                }
                return;
            }

            if (slot == pagination.getPageIndicatorSlot()) {
                return;
            }
            
            for (int s : pagination.getContentSlots()) {
                if (s == slot) {
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem != null && !clickedItem.getType().isAir()) {
                        pagination.handleClick(player, slot, clickedItem);
                    }
                    return;
                }
            }
        }
    }
}
