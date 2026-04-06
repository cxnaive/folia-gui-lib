package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.annotation.GUIConfig;
import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.content.ContentManager;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.CloseEvent;
import com.thenextlvl.foliagui.api.event.OpenEvent;
import com.thenextlvl.foliagui.reactive.ComputedValue;
import com.thenextlvl.foliagui.reactive.ObservableValue;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 声明式GUI抽象基类
 * <p>
 * 支持通过注解定义GUI，并提供响应式数据绑定能力
 * <p>
 * 使用示例：
 * <pre>
 * &#64;GUIConfig(id = "shop", title = "§6商店", rows = 3)
 * public class ShopGUI extends AbstractDeclarativeGUI {
 *
 *     // 响应式状态
 *     private final ObservableValue&lt;Integer&gt; coins = new ObservableValue&lt;&gt;(100);
 *
 *     &#64;Button(slot = 11, material = Material.DIAMOND, name = "§b钻石")
 *     &#64;Action("buy DIAMOND 100")
 *     private Button diamondButton;
 *
 *     &#64;Override
 *     protected void setupBindings() {
 *         // 绑定响应式数据
 *         bind(13, coins, c -&gt; ItemBuilder.of(Material.GOLD_INGOT)
 *             .name("§6金币: " + c)
 *             .build());
 *     }
 *
 *     &#64;ActionHandler("buy")
 *     public void onBuy(Player player, String args) {
 *         // 处理购买逻辑
 *     }
 * }
 * </pre>
 *
 * @author TheNextLvl
 */
public abstract class AbstractDeclarativeGUI implements GUI {

    protected final String id;
    protected GUI builtGUI;
    protected Player viewer;
    protected boolean isOpen = false;

    // 内容管理器
    private ContentManager contentManager;

    // 响应式绑定
    private final Map<Integer, List<Runnable>> slotBindings = new HashMap<>();
    private final List<Runnable> allBindings = new ArrayList<>();

    public AbstractDeclarativeGUI() {
        GUIConfig config = getClass().getAnnotation(GUIConfig.class);
        if (config == null) {
            throw new IllegalStateException("DeclarativeGUI must have @GUIConfig annotation");
        }
        this.id = config.id();
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public void open(@NotNull Player player) {
        if (builtGUI == null) {
            DeclarativeGUIBuilder builder = new DeclarativeGUIBuilder(this);
            builtGUI = builder.build();
            // 构建后设置绑定
            setupBindings();
        }
        viewer = player;
        isOpen = true;
        builtGUI.open(player);
    }

    @Override
    public void close(@NotNull Player player) {
        if (builtGUI != null) {
            builtGUI.close(player);
        }
        if (viewer != null && viewer.equals(player)) {
            isOpen = false;
            viewer = null;
        }
    }

    @Override
    public void refresh() {
        if (builtGUI != null) {
            builtGUI.refresh();
        }
    }

    @Override
    public void updateSlot(int slot) {
        if (builtGUI != null) {
            builtGUI.updateSlot(slot);
        }
    }

    /**
     * 更新指定槽位并触发绑定
     *
     * @param slot 槽位
     */
    protected void refreshSlot(int slot) {
        // 触发绑定更新
        List<Runnable> bindings = slotBindings.get(slot);
        if (bindings != null) {
            for (Runnable binding : bindings) {
                try {
                    binding.run();
                } catch (Exception ignored) {
                }
            }
        }
        // 更新槽位显示
        updateSlot(slot);
    }

    /**
     * 刷新所有绑定的槽位
     */
    protected void refreshAllBindings() {
        for (Runnable binding : allBindings) {
            try {
                binding.run();
            } catch (Exception ignored) {
            }
        }
        refresh();
    }

    @Override
    public void dispose() {
        if (builtGUI != null) {
            builtGUI.dispose();
        }
        isOpen = false;
        viewer = null;
        builtGUI = null;
        // 清除绑定
        slotBindings.clear();
        allBindings.clear();
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
    public @Nullable org.bukkit.Location getLocation() {
        return viewer != null ? viewer.getLocation() : null;
    }

    @Override
    public @Nullable Inventory getInventory() {
        return builtGUI != null ? builtGUI.getInventory() : null;
    }

    @Override
    public GUI onClick(@NotNull Consumer<ClickEvent> handler) {
        if (builtGUI != null) {
            builtGUI.onClick(handler);
        }
        return this;
    }

    @Override
    public GUI onOpen(@NotNull Consumer<OpenEvent> handler) {
        if (builtGUI != null) {
            builtGUI.onOpen(handler);
        }
        return this;
    }

    @Override
    public GUI onClose(@NotNull Consumer<CloseEvent> handler) {
        if (builtGUI != null) {
            builtGUI.onClose(handler);
        }
        return this;
    }

    /**
     * GUI 打开时调用
     * <p>
     * 子类可重写此方法进行初始化
     *
     * @param player 打开 GUI 的玩家
     */
    public void onOpen(@NotNull Player player) {
    }

    /**
     * GUI 关闭时调用
     * <p>
     * 子类可重写此方法进行清理
     *
     * @param player 关闭 GUI 的玩家
     */
    public void onClose(@NotNull Player player) {
    }

    /**
     * 设置响应式数据绑定
     * <p>
     * 子类应重写此方法，使用 bind() 方法设置响应式绑定
     * <pre>
     * &#64;Override
     * protected void setupBindings() {
     *     bind(10, coins, c -&gt; buildCoinsDisplay(c));
     *     bind(11, level, l -&gt; buildLevelDisplay(l));
     * }
     * </pre>
     */
    protected void setupBindings() {
        // 子类重写此方法设置绑定
    }

    // ==================== 响应式绑定方法 ====================

    /**
     * 绑定可观察值到指定槽位
     * <p>
     * 当值变化时自动更新槽位显示
     *
     * @param slot     槽位
     * @param value    可观察值
     * @param renderer 渲染函数，将值转换为 ItemStack
     * @param <T>      值类型
     */
    protected <T> void bind(int slot, @NotNull ObservableValue<T> value,
                            @NotNull java.util.function.Function<T, org.bukkit.inventory.ItemStack> renderer) {
        Runnable binding = () -> {
            if (builtGUI != null && builtGUI.getInventory() != null) {
                T v = value.get();
                org.bukkit.inventory.ItemStack item = renderer.apply(v);
                if (item != null) {
                    builtGUI.getInventory().setItem(slot, item);
                }
            }
        };

        // 添加到槽位绑定列表
        slotBindings.computeIfAbsent(slot, k -> new ArrayList<>()).add(binding);
        allBindings.add(binding);

        // 设置监听器
        value.onChange(v -> {
            binding.run();
            updateSlot(slot);
        });

        // 立即执行一次
        binding.run();
    }

    /**
     * 绑定计算属性到指定槽位
     *
     * @param slot     槽位
     * @param value    计算属性
     * @param renderer 渲染函数
     * @param <T>      值类型
     */
    protected <T> void bind(int slot, @NotNull ComputedValue<T> value,
                            @NotNull java.util.function.Function<T, org.bukkit.inventory.ItemStack> renderer) {
        bind(slot, value.asObservable(), renderer);
    }

    /**
     * 绑定多个可观察值到指定槽位
     * <p>
     * 任一值变化时都会更新槽位
     *
     * @param slot     槽位
     * @param renderer 渲染函数
     * @param values   可观察值数组
     */
    protected void bind(int slot,
                        @NotNull java.util.function.Supplier<org.bukkit.inventory.ItemStack> renderer,
                        @NotNull ObservableValue<?>... values) {
        Runnable binding = () -> {
            if (builtGUI != null && builtGUI.getInventory() != null) {
                org.bukkit.inventory.ItemStack item = renderer.get();
                if (item != null) {
                    builtGUI.getInventory().setItem(slot, item);
                }
            }
        };

        slotBindings.computeIfAbsent(slot, k -> new ArrayList<>()).add(binding);
        allBindings.add(binding);

        // 为每个值设置监听器
        for (ObservableValue<?> v : values) {
            v.onChange(ignored -> {
                binding.run();
                updateSlot(slot);
            });
        }

        // 立即执行一次
        binding.run();
    }

    /**
     * 绑定可观察值到多个槽位
     *
     * @param slots    槽位数组
     * @param value    可观察值
     * @param renderer 渲染函数
     * @param <T>      值类型
     */
    protected <T> void bind(int[] slots, @NotNull ObservableValue<T> value,
                            @NotNull java.util.function.Function<T, org.bukkit.inventory.ItemStack> renderer) {
        for (int slot : slots) {
            bind(slot, value, renderer);
        }
    }

    /**
     * 重建 GUI
     * <p>
     * 清除当前绑定并重新构建
     */
    protected void rebuild() {
        slotBindings.clear();
        allBindings.clear();
        DeclarativeGUIBuilder builder = new DeclarativeGUIBuilder(this);
        builtGUI = builder.build();
        setupBindings();
    }

    /**
     * 获取槽位绑定数量
     *
     * @return 绑定数量
     */
    protected int getBindingCount() {
        return allBindings.size();
    }

    @Override
    @NotNull
    public ContentManager getContentManager() {
        if (contentManager == null) {
            contentManager = new ContentManager();
        }
        return contentManager;
    }

    @Override
    @Nullable
    public Object getSlotData(int slot) {
        if (contentManager != null) {
            return contentManager.getDynamicData(slot);
        }
        return null;
    }

    @Override
    @Nullable
    public <T> T getSlotData(int slot, @NotNull Class<T> type) {
        if (contentManager != null) {
            return contentManager.getDynamicData(slot, type);
        }
        return null;
    }
}