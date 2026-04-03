package com.thenextlvl.foliagui.dialog;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 输入对话框配置
 * <p>
 * 用于配置输入对话框的标题、提示、输入类型和回调
 * <pre>
 * InputDialogConfig config = InputDialogConfig.builder()
 *     .title("&e存入银行")
 *     .prompt("&7请输入存入数量:")
 *     .inputType(InputType.NUMBER)
 *     .maxValue(1000000)
 *     .onSubmit((input) -&gt; {
 *         int amount = Integer.parseInt(input);
 *         player.sendMessage("§a已存入 " + amount + " 金币");
 *     })
 *     .build();
 * </pre>
 *
 * @author TheNextLvl
 */
public class InputDialogConfig {

    private final String title;
    private final String prompt;
    private final InputType inputType;
    private final String placeholder;
    private final String defaultValue;
    private final int minValue;
    private final int maxValue;
    private final int maxLength;
    private final Predicate<String> validator;
    private final String validationErrorMessage;
    private final Consumer<String> onSubmit;
    private final Consumer<Player> onCancel;
    private final Sound successSound;
    private final Sound errorSound;
    private final boolean restoreGUI;

    private InputDialogConfig(Builder builder) {
        this.title = builder.title;
        this.prompt = builder.prompt;
        this.inputType = builder.inputType;
        this.placeholder = builder.placeholder;
        this.defaultValue = builder.defaultValue;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.maxLength = builder.maxLength;
        this.validator = builder.validator;
        this.validationErrorMessage = builder.validationErrorMessage;
        this.onSubmit = builder.onSubmit;
        this.onCancel = builder.onCancel;
        this.successSound = builder.successSound;
        this.errorSound = builder.errorSound;
        this.restoreGUI = builder.restoreGUI;
    }

    /**
     * 创建构建器
     */
    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    // Getters

    @NotNull
    public String getTitle() {
        return title;
    }

    @NotNull
    public String getPrompt() {
        return prompt;
    }

    @NotNull
    public InputType getInputType() {
        return inputType;
    }

    @Nullable
    public String getPlaceholder() {
        return placeholder;
    }

    @Nullable
    public String getDefaultValue() {
        return defaultValue;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public int getMaxLength() {
        return maxLength;
    }

    @Nullable
    public Predicate<String> getValidator() {
        return validator;
    }

    @NotNull
    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    @Nullable
    public Consumer<String> getOnSubmit() {
        return onSubmit;
    }

    @Nullable
    public Consumer<Player> getOnCancel() {
        return onCancel;
    }

    @Nullable
    public Sound getSuccessSound() {
        return successSound;
    }

    @Nullable
    public Sound getErrorSound() {
        return errorSound;
    }

    public boolean isRestoreGUI() {
        return restoreGUI;
    }

    /**
     * 验证输入
     *
     * @param input 用户输入
     * @return 是否有效
     */
    public boolean validate(@NotNull String input) {
        if (input.isEmpty()) {
            return false;
        }

        // 长度检查
        if (maxLength > 0 && input.length() > maxLength) {
            return false;
        }

        // 类型检查
        switch (inputType) {
            case NUMBER -> {
                try {
                    long value = Long.parseLong(input);
                    if (minValue != Integer.MIN_VALUE && value < minValue) return false;
                    if (maxValue != Integer.MAX_VALUE && value > maxValue) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case DECIMAL -> {
                try {
                    double value = Double.parseDouble(input);
                    if (minValue != Integer.MIN_VALUE && value < minValue) return false;
                    if (maxValue != Integer.MAX_VALUE && value > maxValue) return false;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
        }

        // 自定义验证器
        if (validator != null) {
            return validator.test(input);
        }

        return true;
    }

    /**
     * 输入类型
     */
    public enum InputType {
        TEXT,       // 任意文本
        NUMBER,     // 整数
        DECIMAL,    // 小数
        PLAYER_NAME // 玩家名（自动验证玩家是否存在）
    }

    /**
     * 构建器
     */
    public static class Builder {
        private String title = "&8输入";
        private String prompt = "&7请输入:";
        private InputType inputType = InputType.TEXT;
        private String placeholder = null;
        private String defaultValue = null;
        private int minValue = Integer.MIN_VALUE;
        private int maxValue = Integer.MAX_VALUE;
        private int maxLength = 100;
        private Predicate<String> validator = null;
        private String validationErrorMessage = "&c输入格式不正确！";
        private Consumer<String> onSubmit = null;
        private Consumer<Player> onCancel = null;
        private Sound successSound = Sound.ENTITY_PLAYER_LEVELUP;
        private Sound errorSound = Sound.ENTITY_VILLAGER_NO;
        private boolean restoreGUI = true;

        /**
         * 设置标题
         */
        @NotNull
        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置提示文本
         */
        @NotNull
        public Builder prompt(@NotNull String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * 设置输入类型
         */
        @NotNull
        public Builder inputType(@NotNull InputType type) {
            this.inputType = type;
            return this;
        }

        /**
         * 设置占位符
         */
        @NotNull
        public Builder placeholder(@Nullable String placeholder) {
            this.placeholder = placeholder;
            return this;
        }

        /**
         * 设置默认值
         */
        @NotNull
        public Builder defaultValue(@Nullable String value) {
            this.defaultValue = value;
            return this;
        }

        /**
         * 设置最小值（用于 NUMBER/DECIMAL 类型）
         */
        @NotNull
        public Builder minValue(int min) {
            this.minValue = min;
            return this;
        }

        /**
         * 设置最大值（用于 NUMBER/DECIMAL 类型）
         */
        @NotNull
        public Builder maxValue(int max) {
            this.maxValue = max;
            return this;
        }

        /**
         * 设置值范围
         */
        @NotNull
        public Builder range(int min, int max) {
            this.minValue = min;
            this.maxValue = max;
            return this;
        }

        /**
         * 设置最大长度
         */
        @NotNull
        public Builder maxLength(int length) {
            this.maxLength = length;
            return this;
        }

        /**
         * 设置自定义验证器
         */
        @NotNull
        public Builder validator(@Nullable Predicate<String> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * 设置验证失败消息
         */
        @NotNull
        public Builder validationErrorMessage(@NotNull String message) {
            this.validationErrorMessage = message;
            return this;
        }

        /**
         * 设置提交回调
         */
        @NotNull
        public Builder onSubmit(@NotNull Consumer<String> callback) {
            this.onSubmit = callback;
            return this;
        }

        /**
         * 设置取消回调
         */
        @NotNull
        public Builder onCancel(@NotNull Consumer<Player> callback) {
            this.onCancel = callback;
            return this;
        }

        /**
         * 设置成功音效
         */
        @NotNull
        public Builder successSound(@Nullable Sound sound) {
            this.successSound = sound;
            return this;
        }

        /**
         * 设置错误音效
         */
        @NotNull
        public Builder errorSound(@Nullable Sound sound) {
            this.errorSound = sound;
            return this;
        }

        /**
         * 设置提交后是否恢复 GUI
         */
        @NotNull
        public Builder restoreGUI(boolean restore) {
            this.restoreGUI = restore;
            return this;
        }

        /**
         * 构建配置
         */
        @NotNull
        public InputDialogConfig build() {
            return new InputDialogConfig(this);
        }
    }
}