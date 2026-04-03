package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.Slider;
import com.thenextlvl.foliagui.api.event.ClickEvent;
import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 滑块组件实现
 * 单图标组件，在 Lore 中显示滑块轨道
 *
 * @author TheNextLvl
 */
public class SliderImpl implements Slider {

    private final String id;
    private double min = 0.0;
    private double max = 100.0;
    private double value = 50.0;
    private double step = 1.0;

    // 单图标配置
    private ItemStack baseItem;
    private Supplier<ItemStack> baseItemSupplier;
    private int barLength = 20;
    private String thumbChar;
    private String trackChar;
    private String thumbColor = "§a";
    private String trackColor = "§7";
    private String displayName;
    private Style style = Style.BLOCK;
    private boolean showValue = true;
    private String valueFormat = "%.1f";

    // 事件处理
    private Consumer<ValueChangeEvent> valueChangeListener;
    private Consumer<ClickEvent> clickHandler;

    // 组件基础属性
    private int slot = -1;
    private boolean interactable = true;
    private boolean movable = false;

    public SliderImpl(@NotNull String id) {
        this.id = id;
        applyStyleDefaults();
    }

    /**
     * 根据样式设置默认字符
     */
    private void applyStyleDefaults() {
        switch (style) {
            case BLOCK -> { thumbChar = "█"; trackChar = "▓"; }
            case ARROW -> { thumbChar = "►"; trackChar = "="; }
            case DOT -> { thumbChar = "●"; trackChar = "○"; }
            case STAR -> { thumbChar = "★"; trackChar = "☆"; }
            case TEXT -> { thumbChar = "|"; trackChar = "-"; }
            case CUSTOM -> { thumbChar = "■"; trackChar = "□"; }
        }
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public double getMin() {
        return min;
    }

    @Override
    public double getMax() {
        return max;
    }

    @Override
    public Slider range(double min, double max) {
        this.min = min;
        this.max = max;
        this.value = Math.max(min, Math.min(max, this.value));
        return this;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public Slider value(double value) {
        double oldValue = this.value;
        this.value = Math.max(min, Math.min(max, value));

        // 应用步长
        if (step > 0) {
            double steps = Math.round((this.value - min) / step);
            this.value = min + steps * step;
        }

        // 触发变化事件
        if (oldValue != this.value && valueChangeListener != null) {
            ValueChangeEvent event = new ValueChangeEvent(this, oldValue, this.value, null);
            valueChangeListener.accept(event);
        }

        return this;
    }

    @Override
    public double getStep() {
        return step;
    }

    @Override
    public Slider step(double step) {
        this.step = step;
        return this;
    }

    @Override
    public int getBarLength() {
        return barLength;
    }

    @Override
    public Slider barLength(int length) {
        this.barLength = Math.max(1, length);
        return this;
    }

    @Override
    public @NotNull Style getStyle() {
        return style;
    }

    @Override
    public Slider style(@NotNull Style style) {
        this.style = style;
        applyStyleDefaults();
        return this;
    }

    @Override
    public @NotNull String getTrackChar() {
        return trackChar;
    }

    @Override
    public Slider trackChar(@NotNull String charStr) {
        this.trackChar = charStr;
        return this;
    }

    @Override
    public @NotNull String getThumbChar() {
        return thumbChar;
    }

    @Override
    public Slider thumbChar(@NotNull String charStr) {
        this.thumbChar = charStr;
        return this;
    }

    @Override
    public @NotNull String getTrackColor() {
        return trackColor;
    }

    @Override
    public Slider trackColor(@NotNull String color) {
        this.trackColor = color;
        return this;
    }

    @Override
    public @NotNull String getThumbColor() {
        return thumbColor;
    }

    @Override
    public Slider thumbColor(@NotNull String color) {
        this.thumbColor = color;
        return this;
    }

    @Override
    public Slider baseItem(@NotNull ItemStack item) {
        this.baseItem = item.clone();
        this.baseItemSupplier = null;
        return this;
    }

    @Override
    public Slider baseItem(@NotNull Supplier<ItemStack> supplier) {
        this.baseItemSupplier = supplier;
        this.baseItem = null;
        return this;
    }

    @Override
    public Slider displayName(@NotNull String name) {
        this.displayName = name;
        return this;
    }

    @Override
    public Slider showValue(boolean show) {
        this.showValue = show;
        return this;
    }

    @Override
    public boolean isShowValue() {
        return showValue;
    }

    @Override
    public Slider valueFormat(@NotNull String format) {
        this.valueFormat = format;
        return this;
    }

    @Override
    public Slider onValueChange(@NotNull Consumer<ValueChangeEvent> listener) {
        this.valueChangeListener = listener;
        return this;
    }

    @Override
    public @NotNull ItemStack getDisplayItem() {
        ItemStack item = getBaseItem().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // 设置显示名称
        if (displayName != null) {
            meta.setDisplayName(displayName);
        }

        // 生成 Lore：滑块轨道 + 数值
        List<String> lore = new ArrayList<>();
        lore.add(generateSliderBarText());

        if (showValue) {
            String valueText = String.format(valueFormat, value);
            lore.add("§7当前值: §f" + valueText);
        }

        lore.add("§8点击调整数值");

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 生成滑块轨道文本
     * @return 滑块字符串（如 "§7▓▓▓▓§a█§7▓▓▓▓▓▓▓▓▓▓▓▓▓"）
     */
    private @NotNull String generateSliderBarText() {
        double ratio = max <= min ? 0.5 : (value - min) / (max - min);
        int thumbPosition = (int) Math.round(ratio * (barLength - 1));

        StringBuilder bar = new StringBuilder();

        // 轨道前半部分
        bar.append(trackColor);
        for (int i = 0; i < thumbPosition; i++) {
            bar.append(trackChar);
        }

        // 滑块位置
        bar.append(thumbColor);
        bar.append(thumbChar);

        // 轨道后半部分
        bar.append(trackColor);
        for (int i = thumbPosition + 1; i < barLength; i++) {
            bar.append(trackChar);
        }

        return bar.toString();
    }

    /**
     * 获取基础物品
     */
    private @NotNull ItemStack getBaseItem() {
        if (baseItemSupplier != null) {
            return baseItemSupplier.get();
        }
        if (baseItem != null) {
            return baseItem;
        }
        return getDefaultBaseItem();
    }

    /**
     * 获取默认基础物品
     */
    private @NotNull ItemStack getDefaultBaseItem() {
        ItemStack item = new ItemStack(Material.COMPARATOR);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e滑块");
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * 处理点击事件 - 根据 UI 操作调整数值
     * 左键减少，右键增加
     */
    public void handleClick(@NotNull Player player, boolean isRightClick) {
        double oldValue = this.value;
        double delta = isRightClick ? step : -step;
        double newValue = Math.max(min, Math.min(max, this.value + delta));

        if (newValue != oldValue) {
            this.value = newValue;

            if (valueChangeListener != null) {
                ValueChangeEvent event = new ValueChangeEvent(this, oldValue, this.value, player);
                valueChangeListener.accept(event);
                if (event.isCancelled()) {
                    this.value = oldValue;
                }
            }
        }
    }

    @Override
    public void setDisplayItem(@NotNull ItemStack item) {
        this.baseItem = item.clone();
        this.baseItemSupplier = null;
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
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            // 左键减少，右键增加
            boolean isRightClick = event.getClickType().isRightClick();
            handleClick(player, isRightClick);
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

    @Override
    public Component onClick(@NotNull Consumer<ClickEvent> handler) {
        this.clickHandler = handler;
        return this;
    }

    @Override
    public Component permission(@Nullable String permission) {
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return null;
    }

    @Override
    public Component condition(@Nullable Predicate<Player> condition) {
        return this;
    }

    @Override
    public Component refreshInterval(int ticks) {
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return -1; // Slider 刷新由外部控制
    }

    @Override
    public @NotNull Component clone() {
        SliderImpl cloned = new SliderImpl(id);
        cloned.min = this.min;
        cloned.max = this.max;
        cloned.value = this.value;
        cloned.step = this.step;
        cloned.barLength = this.barLength;
        cloned.thumbChar = this.thumbChar;
        cloned.trackChar = this.trackChar;
        cloned.thumbColor = this.thumbColor;
        cloned.trackColor = this.trackColor;
        cloned.displayName = this.displayName;
        cloned.style = this.style;
        cloned.showValue = this.showValue;
        cloned.valueFormat = this.valueFormat;
        cloned.interactable = this.interactable;
        cloned.movable = this.movable;
        cloned.valueChangeListener = this.valueChangeListener;
        cloned.clickHandler = this.clickHandler;
        if (this.baseItem != null) {
            cloned.baseItem = this.baseItem.clone();
        }
        cloned.baseItemSupplier = this.baseItemSupplier;
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
}