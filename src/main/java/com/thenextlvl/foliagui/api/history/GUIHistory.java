package com.thenextlvl.foliagui.api.history;

import com.thenextlvl.foliagui.api.GUI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

/**
 * GUI历史记录接口
 * <p>
 * 管理玩家的GUI导航历史，支持前进、后退和跳转到指定位置。
 *
 * @author TheNextLvl
 */
public interface GUIHistory {

    /**
     * 获取玩家
     * @return 此历史记录所属的玩家
     */
    @NotNull Player getPlayer();

    /**
     * 获取当前位置
     * @return 当前在历史栈中的位置索引
     */
    int getCurrentPosition();

    /**
     * 获取历史记录数量
     * @return 历史记录总数
     */
    int getSize();

    /**
     * 是否可以后退
     * @return 如果有上一页则返回true
     */
    boolean canGoBack();

    /**
     * 是否可以前进
     * @return 如果有下一页则返回true
     */
    boolean canGoForward();

    /**
     * 后退到上一个GUI
     * @return 是否成功后退
     */
    boolean goBack();

    /**
     * 前进到下一个GUI
     * @return 是否成功前进
     */
    boolean goForward();

    /**
     * 跳转到指定位置
     * @param position 目标位置
     * @return 是否成功跳转
     */
    boolean goTo(int position);

    /**
     * 添加新的GUI到历史记录
     * @param gui GUI实例
     * @param title 标题
     * @param icon 图标物品
     */
    void add(@NotNull GUI gui, @NotNull String title, @Nullable org.bukkit.inventory.ItemStack icon);

    /**
     * 添加新的GUI到历史记录（简化版）
     * @param gui GUI实例
     */
    default void add(@NotNull GUI gui) {
        add(gui, gui.getId(), null);
    }

    /**
     * 清除所有历史记录
     */
    void clear();

    /**
     * 获取当前GUI
     * @return 当前GUI，如果没有则返回null
     */
    @Nullable GUI getCurrentGUI();

    /**
     * 获取指定位置的GUI记录
     * @param position 位置索引
     * @return GUI记录，如果不存在则返回null
     */
    @Nullable HistoryEntry getEntry(int position);

    /**
     * 获取所有历史记录
     * @return 历史记录列表
     */
    @NotNull List<HistoryEntry> getAllEntries();

    /**
     * 获取最大历史记录数
     * @return 最大记录数
     */
    int getMaxSize();

    /**
     * 设置最大历史记录数
     * @param maxSize 最大记录数
     */
    void setMaxSize(int maxSize);

    /**
     * 设置后退监听器
     * @param listener 监听器
     * @return 此历史记录
     */
    GUIHistory onBack(@NotNull Consumer<HistoryNavigationEvent> listener);

    /**
     * 设置前进监听器
     * @param listener 监听器
     * @return 此历史记录
     */
    GUIHistory onForward(@NotNull Consumer<HistoryNavigationEvent> listener);

    /**
     * 设置导航监听器（前进或后退）
     * @param listener 监听器
     * @return 此历史记录
     */
    GUIHistory onNavigate(@NotNull Consumer<HistoryNavigationEvent> listener);

    /**
     * 打开历史记录浏览器GUI
     */
    void openBrowser();

    /**
     * 历史记录条目
     */
    interface HistoryEntry {
        /**
         * 获取GUI
         * @return GUI实例
         */
        @NotNull GUI getGUI();

        /**
         * 获取标题
         * @return 标题
         */
        @NotNull String getTitle();

        /**
         * 获取图标
         * @return 图标物品，如果没有则返回null
         */
        @Nullable org.bukkit.inventory.ItemStack getIcon();

        /**
         * 获取打开时间
         * @return 时间戳
         */
        long getTimestamp();

        /**
         * 获取在栈中的位置
         * @return 位置索引
         */
        int getPosition();
    }

    /**
     * 导航事件
     */
    class HistoryNavigationEvent {
        private final GUIHistory history;
        private final HistoryEntry from;
        private final HistoryEntry to;
        private final NavigationDirection direction;
        private boolean cancelled = false;

        public HistoryNavigationEvent(GUIHistory history, HistoryEntry from, HistoryEntry to, NavigationDirection direction) {
            this.history = history;
            this.from = from;
            this.to = to;
            this.direction = direction;
        }

        public GUIHistory getHistory() {
            return history;
        }

        public HistoryEntry getFrom() {
            return from;
        }

        public HistoryEntry getTo() {
            return to;
        }

        public NavigationDirection getDirection() {
            return direction;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * 导航方向
     */
    enum NavigationDirection {
        BACK,
        FORWARD,
        JUMP
    }
}
