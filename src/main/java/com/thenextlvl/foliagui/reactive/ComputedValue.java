package com.thenextlvl.foliagui.reactive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Supplier;

/**
 * 计算属性 - 基于其他 ObservableValue 计算得出的响应式值
 * <p>
 * 当依赖项变化时自动重新计算
 * <pre>
 * ObservableValue&lt;Integer&gt; a = new ObservableValue&lt;&gt;(10);
 * ObservableValue&lt;Integer&gt; b = new ObservableValue&lt;&gt;(20);
 *
 * // 创建计算属性
 * ComputedValue&lt;Integer&gt; sum = ComputedValue.of(
 *     () -&gt; a.get() + b.get(),
 *     a, b
 * );
 *
 * System.out.println(sum.get());  // 输出: 30
 *
 * a.set(15);  // 自动触发重新计算
 * System.out.println(sum.get());  // 输出: 35
 * </pre>
 *
 * @param <T> 值的类型
 * @author TheNextLvl
 */
public class ComputedValue<T> {

    private final Supplier<T> computer;
    private T cachedValue;
    private boolean dirty = true;
    private final ObservableValue<T> observable;

    /**
     * 创建计算属性
     *
     * @param computer     计算函数
     * @param dependencies 依赖的可观察值
     * @param <T>          值类型
     * @return 计算属性
     */
    @NotNull
    @SafeVarargs
    public static <T> ComputedValue<T> of(@NotNull Supplier<T> computer,
                                           @NotNull ObservableValue<?>... dependencies) {
        return new ComputedValue<>(computer, dependencies);
    }

    /**
     * 创建计算属性（带初始依赖项）
     *
     * @param computer     计算函数
     * @param dependencies 依赖的可观察值数组
     * @param <T>          值类型
     * @return 计算属性
     */
    @NotNull
    public static <T> ComputedValue<T> of(@NotNull Supplier<T> computer,
                                           @NotNull Iterable<ObservableValue<?>> dependencies) {
        return new ComputedValue<>(computer, dependencies);
    }

    private ComputedValue(@NotNull Supplier<T> computer,
                          @NotNull ObservableValue<?>[] dependencies) {
        this.computer = computer;
        this.observable = new ObservableValue<>();
        setupDependencies(Arrays.asList(dependencies));
        recompute();
    }

    private ComputedValue(@NotNull Supplier<T> computer,
                          @NotNull Iterable<ObservableValue<?>> dependencies) {
        this.computer = computer;
        this.observable = new ObservableValue<>();
        setupDependencies(dependencies);
        recompute();
    }

    private void setupDependencies(@NotNull Iterable<ObservableValue<?>> dependencies) {
        for (ObservableValue<?> dep : dependencies) {
            dep.onChange(v -> markDirty());
        }
    }

    /**
     * 获取当前值
     * <p>
     * 如果值已标记为脏，会先重新计算
     *
     * @return 当前值
     */
    @Nullable
    public T get() {
        if (dirty) {
            recompute();
        }
        return cachedValue;
    }

    /**
     * 获取底层的 ObservableValue，用于绑定到 UI
     *
     * @return ObservableValue
     */
    @NotNull
    public ObservableValue<T> asObservable() {
        return observable;
    }

    /**
     * 添加值变化监听器
     *
     * @param listener 监听器
     * @return this
     */
    @NotNull
    public ComputedValue<T> onChange(@NotNull java.util.function.Consumer<T> listener) {
        observable.onChange(listener);
        return this;
    }

    /**
     * 标记为需要重新计算
     */
    public void markDirty() {
        this.dirty = true;
        // 可以选择立即重新计算或延迟到下次 get()
        // 这里选择延迟计算以提高性能
    }

    /**
     * 强制立即重新计算
     */
    public void recompute() {
        try {
            T newValue = computer.get();
            if (!java.util.Objects.equals(cachedValue, newValue)) {
                cachedValue = newValue;
                observable.forceSet(newValue);
            }
            dirty = false;
        } catch (Exception e) {
            // 计算失败，保持旧值
            dirty = false;
        }
    }

    /**
     * 检查是否需要重新计算
     *
     * @return 是否脏
     */
    public boolean isDirty() {
        return dirty;
    }
}