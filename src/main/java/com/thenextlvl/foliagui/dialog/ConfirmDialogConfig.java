package com.thenextlvl.foliagui.dialog;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * 确认对话框配置
 * <p>
 * 用于配置确认对话框的标题、消息、按钮文本和回调
 * <pre>
 * ConfirmDialogConfig config = ConfirmDialogConfig.builder()
 *     .title("&c确认删除")
 *     .message("&7确定要删除吗？")
 *     .confirmText("&a确认")
 *     .cancelText("&c取消")
 *     .onConfirm(() -&gt; player.sendMessage("§a已确认"))
 *     .onCancel(() -&gt; player.sendMessage("§7已取消"))
 *     .build();
 * </pre>
 *
 * @author TheNextLvl
 */
public class ConfirmDialogConfig {

    private final String title;
    private final String message;
    private final String confirmText;
    private final String cancelText;
    private final Consumer<Player> onConfirm;
    private final Consumer<Player> onCancel;
    private final Sound confirmSound;
    private final Sound cancelSound;
    private final boolean closeOnConfirm;
    private final boolean closeOnCancel;

    private ConfirmDialogConfig(Builder builder) {
        this.title = builder.title;
        this.message = builder.message;
        this.confirmText = builder.confirmText;
        this.cancelText = builder.cancelText;
        this.onConfirm = builder.onConfirm;
        this.onCancel = builder.onCancel;
        this.confirmSound = builder.confirmSound;
        this.cancelSound = builder.cancelSound;
        this.closeOnConfirm = builder.closeOnConfirm;
        this.closeOnCancel = builder.closeOnCancel;
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
    public String getMessage() {
        return message;
    }

    @NotNull
    public String getConfirmText() {
        return confirmText;
    }

    @NotNull
    public String getCancelText() {
        return cancelText;
    }

    @Nullable
    public Consumer<Player> getOnConfirm() {
        return onConfirm;
    }

    @Nullable
    public Consumer<Player> getOnCancel() {
        return onCancel;
    }

    @Nullable
    public Sound getConfirmSound() {
        return confirmSound;
    }

    @Nullable
    public Sound getCancelSound() {
        return cancelSound;
    }

    public boolean isCloseOnConfirm() {
        return closeOnConfirm;
    }

    public boolean isCloseOnCancel() {
        return closeOnCancel;
    }

    /**
     * 构建器
     */
    public static class Builder {
        private String title = "&8确认";
        private String message = "&7确定要执行此操作吗？";
        private String confirmText = "&a确认";
        private String cancelText = "&c取消";
        private Consumer<Player> onConfirm;
        private Consumer<Player> onCancel;
        private Sound confirmSound = Sound.ENTITY_PLAYER_LEVELUP;
        private Sound cancelSound = Sound.ENTITY_VILLAGER_NO;
        private boolean closeOnConfirm = true;
        private boolean closeOnCancel = true;

        /**
         * 设置标题
         */
        @NotNull
        public Builder title(@NotNull String title) {
            this.title = title;
            return this;
        }

        /**
         * 设置消息内容
         */
        @NotNull
        public Builder message(@NotNull String message) {
            this.message = message;
            return this;
        }

        /**
         * 设置多行消息
         */
        @NotNull
        public Builder message(@NotNull String... lines) {
            this.message = String.join("\n", lines);
            return this;
        }

        /**
         * 设置确认按钮文本
         */
        @NotNull
        public Builder confirmText(@NotNull String text) {
            this.confirmText = text;
            return this;
        }

        /**
         * 设置取消按钮文本
         */
        @NotNull
        public Builder cancelText(@NotNull String text) {
            this.cancelText = text;
            return this;
        }

        /**
         * 设置确认回调
         */
        @NotNull
        public Builder onConfirm(@NotNull Consumer<Player> callback) {
            this.onConfirm = callback;
            return this;
        }

        /**
         * 设置确认回调（无参数版本）
         */
        @NotNull
        public Builder onConfirm(@NotNull Runnable callback) {
            this.onConfirm = p -> callback.run();
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
         * 设置取消回调（无参数版本）
         */
        @NotNull
        public Builder onCancel(@NotNull Runnable callback) {
            this.onCancel = p -> callback.run();
            return this;
        }

        /**
         * 设置确认音效
         */
        @NotNull
        public Builder confirmSound(@Nullable Sound sound) {
            this.confirmSound = sound;
            return this;
        }

        /**
         * 设置取消音效
         */
        @NotNull
        public Builder cancelSound(@Nullable Sound sound) {
            this.cancelSound = sound;
            return this;
        }

        /**
         * 设置确认后是否关闭对话框
         */
        @NotNull
        public Builder closeOnConfirm(boolean close) {
            this.closeOnConfirm = close;
            return this;
        }

        /**
         * 设置取消后是否关闭对话框
         */
        @NotNull
        public Builder closeOnCancel(boolean close) {
            this.closeOnCancel = close;
            return this;
        }

        /**
         * 构建配置
         */
        @NotNull
        public ConfirmDialogConfig build() {
            return new ConfirmDialogConfig(this);
        }
    }
}