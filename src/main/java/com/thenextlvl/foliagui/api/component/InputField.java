package com.thenextlvl.foliagui.api.component;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * 输入框组件接口
 * 用于文本输入和编辑
 *
 * @author TheNextLvl
 */
public interface InputField extends Component {

    /**
     * 获取当前文本
     * @return 文本内容
     */
    @NotNull String getText();

    /**
     * 设置文本
     * @param text 文本内容
     * @return 此组件
     */
    InputField text(@NotNull String text);

    /**
     * 获取占位符文本
     * @return 占位符
     */
    @Nullable String getPlaceholder();

    /**
     * 设置占位符文本
     * @param placeholder 占位符
     * @return 此组件
     */
    InputField placeholder(@Nullable String placeholder);

    /**
     * 获取最大长度
     * @return 最大长度，-1表示无限制
     */
    int getMaxLength();

    /**
     * 设置最大长度
     * @param maxLength 最大长度，-1表示无限制
     * @return 此组件
     */
    InputField maxLength(int maxLength);

    /**
     * 获取输入验证器
     * @return 验证器
     */
    @Nullable Predicate<String> getValidator();

    /**
     * 设置输入验证器
     * @param validator 验证器
     * @return 此组件
     */
    InputField validator(@Nullable Predicate<String> validator);

    /**
     * 获取输入模式
     * @return 输入模式
     */
    @NotNull InputMode getInputMode();

    /**
     * 设置输入模式
     * @param mode 输入模式
     * @return 此组件
     */
    InputField inputMode(@NotNull InputMode mode);

    /**
     * 获取正常状态物品
     * @return 物品
     */
    @NotNull ItemStack getNormalItem();

    /**
     * 设置正常状态物品
     * @param item 物品
     * @return 此组件
     */
    InputField normalItem(@NotNull ItemStack item);

    /**
     * 设置正常状态物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    InputField normalItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 获取焦点状态物品
     * @return 物品
     */
    @NotNull ItemStack getFocusItem();

    /**
     * 设置焦点状态物品
     * @param item 物品
     * @return 此组件
     */
    InputField focusItem(@NotNull ItemStack item);

    /**
     * 设置焦点状态物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    InputField focusItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 获取错误状态物品
     * @return 物品
     */
    @NotNull ItemStack getErrorItem();

    /**
     * 设置错误状态物品
     * @param item 物品
     * @return 此组件
     */
    InputField errorItem(@NotNull ItemStack item);

    /**
     * 设置错误状态物品（动态）
     * @param supplier 物品供应器
     * @return 此组件
     */
    InputField errorItem(@NotNull Supplier<ItemStack> supplier);

    /**
     * 设置文本变化监听器
     * @param listener 监听器
     * @return 此组件
     */
    InputField onTextChange(@NotNull Consumer<TextChangeEvent> listener);

    /**
     * 设置提交监听器
     * @param listener 监听器
     * @return 此组件
     */
    InputField onSubmit(@NotNull Consumer<SubmitEvent> listener);

    /**
     * 检查是否有焦点
     * @return 是否有焦点
     */
    boolean hasFocus();

    /**
     * 设置焦点状态
     * @param focus 是否有焦点
     */
    void setFocus(boolean focus);

    /**
     * 检查当前文本是否有效
     * @return 是否有效
     */
    boolean isValid();

    /**
     * 提交输入
     * @param player 玩家
     * @return 是否提交成功
     */
    boolean submit(@Nullable Player player);

    /**
     * 输入模式
     */
    enum InputMode {
        /**
         * 聊天输入 - 通过聊天栏输入
         */
        CHAT,

        /**
         * 铁砧输入 - 通过铁砧界面输入
         */
        ANVIL,

        /**
         * 符号输入 - 通过点击符号按钮输入
         */
        SIGN,

        /**
         * 虚拟键盘 - 通过GUI虚拟键盘输入
         */
        VIRTUAL_KEYBOARD
    }

    /**
     * 文本变化事件
     */
    class TextChangeEvent {
        private final InputField inputField;
        private final String oldText;
        private final String newText;
        private final Player player;

        public TextChangeEvent(@NotNull InputField inputField, @NotNull String oldText, 
                               @NotNull String newText, @Nullable Player player) {
            this.inputField = inputField;
            this.oldText = oldText;
            this.newText = newText;
            this.player = player;
        }

        public @NotNull InputField getInputField() {
            return inputField;
        }

        public @NotNull String getOldText() {
            return oldText;
        }

        public @NotNull String getNewText() {
            return newText;
        }

        public @Nullable Player getPlayer() {
            return player;
        }
    }

    /**
     * 提交事件
     */
    class SubmitEvent {
        private final InputField inputField;
        private final String text;
        private final Player player;
        private boolean cancelled = false;

        public SubmitEvent(@NotNull InputField inputField, @NotNull String text, @Nullable Player player) {
            this.inputField = inputField;
            this.text = text;
            this.player = player;
        }

        public @NotNull InputField getInputField() {
            return inputField;
        }

        public @NotNull String getText() {
            return text;
        }

        public @Nullable Player getPlayer() {
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
