package com.thenextlvl.foliagui.api.input;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 聊天输入请求接口
 * <p>
 * 表示一个等待玩家输入的请求
 *
 * @author TheNextLvl
 */
public interface ChatInputRequest {

    /**
     * 获取请求ID
     * @return 唯一标识符
     */
    @NotNull String getId();

    /**
     * 获取玩家
     * @return 玩家实例
     */
    @NotNull Player getPlayer();

    /**
     * 获取提示消息
     * @return 提示消息
     */
    @NotNull String getPrompt();

    /**
     * 获取取消关键词
     * @return 取消关键词列表
     */
    @NotNull String[] getCancelKeywords();

    /**
     * 检查是否已取消
     * @return 是否已取消
     */
    boolean isCancelled();

    /**
     * 取消此输入请求
     */
    void cancel();

    /**
     * 获取超时时间（毫秒）
     * @return 超时时间，0表示无超时
     */
    long getTimeout();

    /**
     * 获取剩余时间（毫秒）
     * @return 剩余时间，-1表示已过期或无超时
     */
    long getRemainingTime();

    /**
     * 输入结果
     */
    class InputResult {
        private final String input;
        private final boolean cancelled;
        private final boolean timedOut;
        private final Player player;

        public InputResult(@Nullable String input, boolean cancelled, boolean timedOut, @NotNull Player player) {
            this.input = input;
            this.cancelled = cancelled;
            this.timedOut = timedOut;
            this.player = player;
        }

        /**
         * 获取输入内容
         * @return 输入内容，如果取消或超时则为null
         */
        @Nullable
        public String getInput() {
            return input;
        }

        /**
         * 检查是否被取消
         * @return 是否被取消
         */
        public boolean isCancelled() {
            return cancelled;
        }

        /**
         * 检查是否超时
         * @return 是否超时
         */
        public boolean isTimedOut() {
            return timedOut;
        }

        /**
         * 检查是否成功获取输入
         * @return 是否成功
         */
        public boolean isSuccess() {
            return input != null && !cancelled && !timedOut;
        }

        /**
         * 获取玩家
         * @return 玩家实例
         */
        @NotNull
        public Player getPlayer() {
            return player;
        }
    }

    /**
     * 输入请求构建器
     */
    interface Builder {
        /**
         * 设置提示消息
         * @param prompt 提示消息
         * @return 此构建器
         */
        @NotNull Builder prompt(@NotNull String prompt);

        /**
         * 设置取消关键词
         * @param keywords 取消关键词
         * @return 此构建器
         */
        @NotNull Builder cancelKeywords(@NotNull String... keywords);

        /**
         * 设置超时时间
         * @param milliseconds 超时毫秒数
         * @return 此构建器
         */
        @NotNull Builder timeout(long milliseconds);

        /**
         * 设置输入验证器
         * @param validator 验证器
         * @return 此构建器
         */
        @NotNull Builder validator(@NotNull Predicate<String> validator);

        /**
         * 设置验证失败消息
         * @param message 消息
         * @return 此构建器
         */
        @NotNull Builder validationErrorMessage(@NotNull String message);

        /**
         * 设置是否恢复之前的GUI
         * @param restore 是否恢复
         * @return 此构建器
         */
        @NotNull Builder restoreGUI(boolean restore);

        /**
         * 设置输入完成后的回调
         * @param callback 回调函数
         * @return 此构建器
         */
        @NotNull Builder onComplete(@NotNull Consumer<InputResult> callback);

        /**
         * 设置取消时的回调
         * @param callback 回调函数
         * @return 此构建器
         */
        @NotNull Builder onCancel(@NotNull Runnable callback);

        /**
         * 设置超时时的回调
         * @param callback 回调函数
         * @return 此构建器
         */
        @NotNull Builder onTimeout(@NotNull Runnable callback);

        /**
         * 构建并提交输入请求
         * @return 输入请求实例
         */
        @NotNull ChatInputRequest submit();
    }
}
