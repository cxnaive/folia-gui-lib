package com.thenextlvl.foliagui.api.component;

import com.thenextlvl.foliagui.api.event.InteractionEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 搜索框组件接口
 * 
 * @author TheNextLvl
 */
public interface SearchField extends Component {
    
    /**
     * 获取当前搜索文本
     * @return 搜索文本
     */
    @NotNull String getSearchText();
    
    /**
     * 设置搜索文本
     * @param text 搜索文本
     * @return 此搜索框
     */
    SearchField searchText(@NotNull String text);
    
    /**
     * 获取占位符文本
     * @return 占位符文本，如果没有则返回null
     */
    @Nullable String getPlaceholder();
    
    /**
     * 设置占位符文本
     * @param placeholder 占位符文本
     * @return 此搜索框
     */
    SearchField placeholder(@Nullable String placeholder);
    
    /**
     * 获取最大长度
     * @return 最大长度，-1表示无限制
     */
    int getMaxLength();
    
    /**
     * 设置最大长度
     * @param maxLength 最大长度，-1表示无限制
     * @return 此搜索框
     */
    SearchField maxLength(int maxLength);
    
    /**
     * 获取搜索延迟（毫秒）
     * @return 搜索延迟
     */
    long getSearchDelay();
    
    /**
     * 设置搜索延迟（毫秒）
     * @param delay 延迟时间
     * @return 此搜索框
     */
    SearchField searchDelay(long delay);
    
    /**
     * 是否实时搜索
     * @return 是否实时搜索
     */
    boolean isRealtimeSearch();
    
    /**
     * 设置是否实时搜索
     * @param realtime 是否实时
     * @return 此搜索框
     */
    SearchField realtimeSearch(boolean realtime);
    
    /**
     * 获取搜索过滤器
     * @return 过滤器
     */
    @Nullable Predicate<String> getFilter();
    
    /**
     * 设置搜索过滤器
     * @param filter 过滤器
     * @return 此搜索框
     */
    SearchField filter(@Nullable Predicate<String> filter);
    
    /**
     * 获取正常状态物品
     * @return 物品
     */
    @NotNull ItemStack getNormalItem();
    
    /**
     * 设置正常状态物品
     * @param item 物品
     * @return 此搜索框
     */
    SearchField normalItem(@NotNull ItemStack item);
    
    /**
     * 获取激活状态物品
     * @return 物品
     */
    @NotNull ItemStack getActiveItem();
    
    /**
     * 设置激活状态物品
     * @param item 物品
     * @return 此搜索框
     */
    SearchField activeItem(@NotNull ItemStack item);
    
    /**
     * 获取清除按钮物品
     * @return 物品
     */
    @Nullable ItemStack getClearButtonItem();
    
    /**
     * 设置清除按钮物品
     * @param item 物品
     * @return 此搜索框
     */
    SearchField clearButtonItem(@Nullable ItemStack item);
    
    /**
     * 设置搜索文本变化监听器
     * @param listener 监听器
     * @return 此搜索框
     */
    SearchField onSearchChange(@NotNull Consumer<SearchChangeEvent> listener);
    
    /**
     * 设置搜索提交监听器
     * @param listener 监听器
     * @return 此搜索框
     */
    SearchField onSearchSubmit(@NotNull Consumer<SearchSubmitEvent> listener);
    
    /**
     * 执行搜索
     * @param player 玩家
     */
    void performSearch(@Nullable Player player);
    
    /**
     * 清除搜索
     * @param player 玩家
     */
    void clear(@Nullable Player player);
    
    /**
     * 搜索文本变化事件
     */
    class SearchChangeEvent {
        private final SearchField searchField;
        private final String oldText;
        private final String newText;
        private final Player player;
        private boolean cancelled = false;
        
        public SearchChangeEvent(SearchField searchField, String oldText, String newText, Player player) {
            this.searchField = searchField;
            this.oldText = oldText;
            this.newText = newText;
            this.player = player;
        }
        
        public SearchField getSearchField() {
            return searchField;
        }
        
        public String getOldText() {
            return oldText;
        }
        
        public String getNewText() {
            return newText;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
    
    /**
     * 搜索提交事件
     */
    class SearchSubmitEvent {
        private final SearchField searchField;
        private final String searchText;
        private final Player player;
        private boolean cancelled = false;
        
        public SearchSubmitEvent(SearchField searchField, String searchText, Player player) {
            this.searchField = searchField;
            this.searchText = searchText;
            this.player = player;
        }
        
        public SearchField getSearchField() {
            return searchField;
        }
        
        public String getSearchText() {
            return searchText;
        }
        
        public Player getPlayer() {
            return player;
        }
        
        public boolean isCancelled() {
            return cancelled;
        }
        
        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }
}
