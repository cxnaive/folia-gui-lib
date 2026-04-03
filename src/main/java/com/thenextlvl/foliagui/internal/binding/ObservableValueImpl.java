package com.thenextlvl.foliagui.internal.binding;

import com.thenextlvl.foliagui.api.binding.ObservableValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * ObservableValue 实现类
 *
 * @param <T> 值类型
 * @author TheNextLvl
 */
public class ObservableValueImpl<T> implements ObservableValue<T> {

    private T value;
    private final List<Consumer<ValueChangeEvent<T>>> listeners = new ArrayList<>();
    private Supplier<T> boundSupplier;

    public ObservableValueImpl() {
        this.value = null;
    }

    public ObservableValueImpl(@Nullable T initialValue) {
        this.value = initialValue;
    }

    @Override
    public @Nullable T get() {
        return value;
    }

    @Override
    public void set(@Nullable T value) {
        if (equalsValue(value)) {
            return;
        }

        T oldValue = this.value;
        this.value = value;

        fireChangeEvent(oldValue, value);
    }

    @Override
    public @NotNull ObservableValue<T> onChange(@NotNull Consumer<ValueChangeEvent<T>> listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public @NotNull ObservableValue<T> removeListener(@NotNull Consumer<ValueChangeEvent<T>> listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public @NotNull <R> ObservableValue<R> map(@NotNull Function<T, R> mapper) {
        ObservableValueImpl<R> mapped = new ObservableValueImpl<>(mapper.apply(value));

        onChange(event -> {
            R newValue = mapper.apply(event.getNewValue());
            mapped.set(newValue);
        });

        return mapped;
    }

    @Override
    public void bindBidirectional(@NotNull ObservableValue<T> other) {
        // 当 this 变化时，更新 other
        onChange(event -> {
            if (!other.equalsValue(event.getNewValue())) {
                other.set(event.getNewValue());
            }
        });

        // 当 other 变化时，更新 this
        other.onChange(event -> {
            if (!equalsValue(event.getNewValue())) {
                set(event.getNewValue());
            }
        });
    }

    @Override
    public void bindTo(@NotNull Supplier<T> supplier) {
        this.boundSupplier = supplier;
    }

    /**
     * 从绑定的 Supplier 刷新值
     */
    public void refreshFromBinding() {
        if (boundSupplier != null) {
            set(boundSupplier.get());
        }
    }

    /**
     * 触发值变化事件
     */
    private void fireChangeEvent(@Nullable T oldValue, @Nullable T newValue) {
        ValueChangeEvent<T> event = new ValueChangeEvent<>(this, oldValue, newValue);
        for (Consumer<ValueChangeEvent<T>> listener : new ArrayList<>(listeners)) {
            listener.accept(event);
        }
    }

    /**
     * 创建一个 ObservableValue
     */
    public static <T> ObservableValue<T> of(@Nullable T value) {
        return new ObservableValueImpl<>(value);
    }

    /**
     * 创建一个空的 ObservableValue
     */
    public static <T> ObservableValue<T> empty() {
        return new ObservableValueImpl<>();
    }
}