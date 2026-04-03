package com.thenextlvl.foliagui.api.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 可观察值接口
 * <p>
 * 支持值变化监听和自动更新 GUI 组件
 *
 * @param <T> 值类型
 * @author TheNextLvl
 */
public interface ObservableValue<T> {

    /**
     * 获取当前值
     * @return 当前值
     */
    @Nullable T get();

    /**
     * 设置值
     * @param value 新值
     */
    void set(@Nullable T value);

    /**
     * 添加值变化监听器
     * @param listener 监听器
     * @return 此对象
     */
    @NotNull ObservableValue<T> onChange(@NotNull Consumer<ValueChangeEvent<T>> listener);

    /**
     * 移除值变化监听器
     * @param listener 监听器
     * @return 此对象
     */
    @NotNull ObservableValue<T> removeListener(@NotNull Consumer<ValueChangeEvent<T>> listener);

    /**
     * 清除所有监听器
     */
    void clearListeners();

    /**
     * 映射到另一个类型的 ObservableValue
     * @param mapper 映射函数
     * @param <R> 目标类型
     * @return 新的 ObservableValue
     */
    @NotNull <R> ObservableValue<R> map(@NotNull Function<T, R> mapper);

    /**
     * 双向绑定到另一个 ObservableValue
     * @param other 另一个 ObservableValue
     */
    void bindBidirectional(@NotNull ObservableValue<T> other);

    /**
     * 单向绑定到 Supplier
     * @param supplier 值供应器
     */
    void bindTo(@NotNull Supplier<T> supplier);

    /**
     * 是否与另一个值相等
     * @param other 另一个值
     * @return 是否相等
     */
    default boolean equalsValue(@Nullable T other) {
        T current = get();
        return current == null ? other == null : current.equals(other);
    }

    /**
     * 获取值或默认值
     * @param defaultValue 默认值
     * @return 值或默认值
     */
    default T getOrElse(@NotNull T defaultValue) {
        T value = get();
        return value != null ? value : defaultValue;
    }

    /**
     * 值变化事件
     */
    class ValueChangeEvent<T> {
        private final ObservableValue<T> source;
        private final T oldValue;
        private final T newValue;

        public ValueChangeEvent(@NotNull ObservableValue<T> source, @Nullable T oldValue, @Nullable T newValue) {
            this.source = source;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public @NotNull ObservableValue<T> getSource() {
            return source;
        }

        public @Nullable T getOldValue() {
            return oldValue;
        }

        public @Nullable T getNewValue() {
            return newValue;
        }
    }
}