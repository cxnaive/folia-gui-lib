package com.thenextlvl.foliagui.internal;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.api.component.ProgressBar;
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
 * 进度条组件实现
 * 单图标组件，在 Lore 中显示文本进度条
 *
 * @author TheNextLvl
 */
public class ProgressBarImpl implements ProgressBar {

    private final String id;
    private double min = 0.0;
    private double max = 100.0;
    private double value = 0.0;

    // 单图标配置
    private ItemStack baseItem;
    private Supplier<ItemStack> baseItemSupplier;
    private int barLength = 20;
    private String filledChar;
    private String emptyChar;
    private String filledColor = "§a";
    private String emptyColor = "§7";
    private String displayName;
    private Style style = Style.BLOCK;
    private boolean showPercentage = true;
    private String percentageFormat = "%.1f%%";

    // 组件基础属性
    private int slot = -1;
    private boolean interactable = false;
    private boolean movable = false;
    private Consumer<ClickEvent> clickHandler;

    public ProgressBarImpl(@NotNull String id) {
        this.id = id;
        applyStyleDefaults();
    }

    /**
     * 根据样式设置默认字符
     */
    private void applyStyleDefaults() {
        switch (style) {
            case BLOCK -> { filledChar = "█"; emptyChar = "░"; }
            case ARROW -> { filledChar = "►"; emptyChar = "◇"; }
            case STAR -> { filledChar = "★"; emptyChar = "☆"; }
            case DOT -> { filledChar = "●"; emptyChar = "○"; }
            case HEART -> { filledChar = "❤"; emptyChar = "♡"; }
            case TEXT -> { filledChar = "="; emptyChar = "-"; }
            case CUSTOM -> { filledChar = "■"; emptyChar = "□"; }
        }
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public double getProgress() {
        if (max <= min) return 0.0;
        double progress = (value - min) / (max - min);
        return Math.max(0.0, Math.min(1.0, progress));
    }

    @Override
    public void setProgress(double progress) {
        this.value = min + (max - min) * Math.max(0.0, Math.min(1.0, progress));
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
    public ProgressBar range(double min, double max) {
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
    public ProgressBar value(double value) {
        this.value = Math.max(min, Math.min(max, value));
        return this;
    }

    @Override
    public int getBarLength() {
        return barLength;
    }

    @Override
    public ProgressBar barLength(int length) {
        this.barLength = Math.max(1, length);
        return this;
    }

    @Override
    public @NotNull Style getStyle() {
        return style;
    }

    @Override
    public ProgressBar style(@NotNull Style style) {
        this.style = style;
        applyStyleDefaults();
        return this;
    }

    @Override
    public @NotNull String getFilledChar() {
        return filledChar;
    }

    @Override
    public ProgressBar filledChar(@NotNull String charStr) {
        this.filledChar = charStr;
        return this;
    }

    @Override
    public @NotNull String getEmptyChar() {
        return emptyChar;
    }

    @Override
    public ProgressBar emptyChar(@NotNull String charStr) {
        this.emptyChar = charStr;
        return this;
    }

    @Override
    public @NotNull String getFilledColor() {
        return filledColor;
    }

    @Override
    public ProgressBar filledColor(@NotNull String color) {
        this.filledColor = color;
        return this;
    }

    @Override
    public @NotNull String getEmptyColor() {
        return emptyColor;
    }

    @Override
    public ProgressBar emptyColor(@NotNull String color) {
        this.emptyColor = color;
        return this;
    }

    @Override
    public ProgressBar baseItem(@NotNull ItemStack item) {
        this.baseItem = item.clone();
        this.baseItemSupplier = null;
        return this;
    }

    @Override
    public ProgressBar baseItem(@NotNull Supplier<ItemStack> supplier) {
        this.baseItemSupplier = supplier;
        this.baseItem = null;
        return this;
    }

    @Override
    public ProgressBar displayName(@NotNull String name) {
        this.displayName = name;
        return this;
    }

    @Override
    public ProgressBar showPercentage(boolean show) {
        this.showPercentage = show;
        return this;
    }

    @Override
    public boolean isShowPercentage() {
        return showPercentage;
    }

    @Override
    public ProgressBar percentageFormat(@NotNull String format) {
        this.percentageFormat = format;
        return this;
    }

    /**
     * 获取百分比格式
     * @return 格式字符串
     */
    public @NotNull String getPercentageFormat() {
        return percentageFormat;
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

        // 生成 Lore：进度条文本 + 百分比
        List<String> lore = new ArrayList<>();
        lore.add(generateProgressBarText());

        if (showPercentage) {
            String percentageText = String.format(percentageFormat, getProgress() * 100);
            lore.add("§7" + percentageText);
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * 生成进度条文本
     * @return 进度条字符串（如 "§a████████░░░░"）
     */
    private @NotNull String generateProgressBarText() {
        double progress = getProgress();
        int filledCount = (int) Math.round(progress * barLength);
        int emptyCount = barLength - filledCount;

        StringBuilder bar = new StringBuilder();
        bar.append(filledColor);
        for (int i = 0; i < filledCount; i++) {
            bar.append(filledChar);
        }
        bar.append(emptyColor);
        for (int i = 0; i < emptyCount; i++) {
            bar.append(emptyChar);
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
        ItemStack item = new ItemStack(Material.EXPERIENCE_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b进度条");
            item.setItemMeta(meta);
        }
        return item;
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
        this.interactable = true;
        return this;
    }

    @Override
    public Component permission(@Nullable String permission) {
        // 进度条不需要权限检查
        return this;
    }

    @Override
    public @Nullable Predicate<Player> getCondition() {
        return null;
    }

    @Override
    public Component condition(@Nullable Predicate<Player> condition) {
        // 进度条不需要条件检查
        return this;
    }

    @Override
    public Component refreshInterval(int ticks) {
        // 进度条刷新由外部控制
        return this;
    }

    @Override
    public int getRefreshInterval() {
        return -1; // 进度条刷新由外部控制
    }

    @Override
    public @NotNull Component clone() {
        ProgressBarImpl cloned = new ProgressBarImpl(id);
        cloned.min = this.min;
        cloned.max = this.max;
        cloned.value = this.value;
        cloned.barLength = this.barLength;
        cloned.filledChar = this.filledChar;
        cloned.emptyChar = this.emptyChar;
        cloned.filledColor = this.filledColor;
        cloned.emptyColor = this.emptyColor;
        cloned.displayName = this.displayName;
        cloned.style = this.style;
        cloned.showPercentage = this.showPercentage;
        cloned.percentageFormat = this.percentageFormat;
        cloned.interactable = this.interactable;
        cloned.movable = this.movable;
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