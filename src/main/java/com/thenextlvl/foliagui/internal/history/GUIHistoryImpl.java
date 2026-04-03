package com.thenextlvl.foliagui.internal.history;

import com.thenextlvl.foliagui.api.GUI;
import com.thenextlvl.foliagui.api.history.GUIHistory;
import com.thenextlvl.foliagui.builder.GUIBuilder;
import com.thenextlvl.foliagui.builder.ItemBuilder;
import com.thenextlvl.foliagui.folia.FoliaScheduler;
import com.thenextlvl.foliagui.manager.GUIManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * GUI历史记录实现类
 * <p>
 * 管理玩家的GUI导航历史，支持前进、后退和跳转
 *
 * @author TheNextLvl
 */
public class GUIHistoryImpl implements GUIHistory {

    private final Player player;
    private final LinkedList<HistoryEntryImpl> entries = new LinkedList<>();
    private int currentPosition = -1;
    private int maxSize = 10;

    // 导航监听器
    private Consumer<HistoryNavigationEvent> backListener;
    private Consumer<HistoryNavigationEvent> forwardListener;
    private Consumer<HistoryNavigationEvent> navigateListener;

    public GUIHistoryImpl(@NotNull Player player) {
        this.player = player;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public int getCurrentPosition() {
        return currentPosition;
    }

    @Override
    public int getSize() {
        return entries.size();
    }

    @Override
    public boolean canGoBack() {
        return currentPosition > 0;
    }

    @Override
    public boolean canGoForward() {
        return currentPosition >= 0 && currentPosition < entries.size() - 1;
    }

    @Override
    public boolean goBack() {
        if (!canGoBack()) {
            return false;
        }

        HistoryEntryImpl from = getCurrentEntry();
        currentPosition--;
        HistoryEntryImpl to = getCurrentEntry();

        // 触发导航事件
        HistoryNavigationEvent event = new HistoryNavigationEvent(this, from, to, NavigationDirection.BACK);

        if (backListener != null) {
            backListener.accept(event);
        }
        if (navigateListener != null) {
            navigateListener.accept(event);
        }

        if (event.isCancelled()) {
            currentPosition++; // 回滚位置
            return false;
        }

        // 打开目标GUI
        openEntry(to);
        return true;
    }

    @Override
    public boolean goForward() {
        if (!canGoForward()) {
            return false;
        }

        HistoryEntryImpl from = getCurrentEntry();
        currentPosition++;
        HistoryEntryImpl to = getCurrentEntry();

        // 触发导航事件
        HistoryNavigationEvent event = new HistoryNavigationEvent(this, from, to, NavigationDirection.FORWARD);

        if (forwardListener != null) {
            forwardListener.accept(event);
        }
        if (navigateListener != null) {
            navigateListener.accept(event);
        }

        if (event.isCancelled()) {
            currentPosition--; // 回滚位置
            return false;
        }

        // 打开目标GUI
        openEntry(to);
        return true;
    }

    @Override
    public boolean goTo(int position) {
        if (position < 0 || position >= entries.size() || position == currentPosition) {
            return false;
        }

        HistoryEntryImpl from = getCurrentEntry();
        int oldPosition = currentPosition;
        currentPosition = position;
        HistoryEntryImpl to = getCurrentEntry();

        // 触发导航事件
        HistoryNavigationEvent event = new HistoryNavigationEvent(this, from, to, NavigationDirection.JUMP);

        if (navigateListener != null) {
            navigateListener.accept(event);
        }

        if (event.isCancelled()) {
            currentPosition = oldPosition; // 回滚位置
            return false;
        }

        // 打开目标GUI
        openEntry(to);
        return true;
    }

    @Override
    public void add(@NotNull GUI gui, @NotNull String title, @Nullable ItemStack icon) {
        // 清除当前位置之后的所有记录（类似于浏览器历史）
        while (entries.size() > currentPosition + 1) {
            entries.removeLast();
        }

        // 创建新的历史条目
        HistoryEntryImpl entry = new HistoryEntryImpl(
                gui,
                title,
                icon != null ? icon.clone() : null,
                System.currentTimeMillis(),
                entries.size()
        );

        entries.add(entry);
        currentPosition = entries.size() - 1;

        // 检查最大限制
        while (entries.size() > maxSize) {
            entries.removeFirst();
            currentPosition--;
            // 更新位置索引
            for (int i = 0; i < entries.size(); i++) {
                entries.get(i).position = i;
            }
        }
    }

    @Override
    public void clear() {
        entries.clear();
        currentPosition = -1;
    }

    @Override
    public @Nullable GUI getCurrentGUI() {
        HistoryEntryImpl entry = getCurrentEntry();
        return entry != null ? entry.getGUI() : null;
    }

    @Override
    public @Nullable HistoryEntry getEntry(int position) {
        if (position < 0 || position >= entries.size()) {
            return null;
        }
        return entries.get(position);
    }

    @Override
    public @NotNull List<HistoryEntry> getAllEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void setMaxSize(int maxSize) {
        this.maxSize = Math.max(1, maxSize);
        // 如果当前历史超过新限制，截断
        while (entries.size() > this.maxSize) {
            entries.removeFirst();
            currentPosition--;
        }
        // 更新位置索引
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).position = i;
        }
    }

    @Override
    public GUIHistory onBack(@NotNull Consumer<HistoryNavigationEvent> listener) {
        this.backListener = listener;
        return this;
    }

    @Override
    public GUIHistory onForward(@NotNull Consumer<HistoryNavigationEvent> listener) {
        this.forwardListener = listener;
        return this;
    }

    @Override
    public GUIHistory onNavigate(@NotNull Consumer<HistoryNavigationEvent> listener) {
        this.navigateListener = listener;
        return this;
    }

    @Override
    public void openBrowser() {
        HistoryBrowserGUI browser = new HistoryBrowserGUI(this);
        browser.open();
    }

    // ==================== 内部方法 ====================

    private @Nullable HistoryEntryImpl getCurrentEntry() {
        if (currentPosition < 0 || currentPosition >= entries.size()) {
            return null;
        }
        return entries.get(currentPosition);
    }

    private void openEntry(@NotNull HistoryEntryImpl entry) {
        FoliaScheduler.runOnPlayer(player, () -> {
            GUI gui = entry.getGUI();
            if (gui != null) {
                GUIManager manager = GUIManager.getInstance();
                if (manager != null) {
                    manager.open(player, gui);
                } else {
                    gui.open(player);
                }
            }
        });
    }

    // ==================== 内部类 ====================

    /**
     * 历史条目实现
     */
    public static class HistoryEntryImpl implements HistoryEntry {
        private final GUI gui;
        private final String title;
        private final ItemStack icon;
        private final long timestamp;
        private int position;

        public HistoryEntryImpl(@NotNull GUI gui, @NotNull String title,
                                @Nullable ItemStack icon, long timestamp, int position) {
            this.gui = gui;
            this.title = title;
            this.icon = icon;
            this.timestamp = timestamp;
            this.position = position;
        }

        @Override
        public @NotNull GUI getGUI() {
            return gui;
        }

        @Override
        public @NotNull String getTitle() {
            return title;
        }

        @Override
        public @Nullable ItemStack getIcon() {
            return icon;
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }

        @Override
        public int getPosition() {
            return position;
        }
    }
}
