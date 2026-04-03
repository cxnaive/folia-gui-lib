package com.thenextlvl.foliagui.reactive;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * 可观察值 - 响应式数据绑定的基础类
 * <p>
 * 当值变化时自动通知所有监听器
 * <pre>
 * // 创建可观察值
 * ObservableValue&lt;Integer&gt; coins = new ObservableValue&lt;&gt;(100);
 *
 * // 添加监听器
 * coins.onChange(newCoins -&gt; {
 *     System.out.println("金币变化: " + newCoins);
 * });
 *
 * // 修改值（自动触发监听器）
 * coins.set(200);  // 输出: 金币变化: 200
 * </pre>
 *
 * @param <T> 值的类型
 * @author TheNextLvl
 */
public class ObservableValue<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();
    private final List<Consumer<Change<T>>> changeListeners = new ArrayList<>();

    /**
     * 创建可观察值，初始值为 null
     */
    public ObservableValue() {
        this.value = null;
    }

    /**
     * 创建可观察值，指定初始值
     *
     * @param initialValue 初始值
     */
    public ObservableValue(@Nullable T initialValue) {
        this.value = initialValue;
    }

    /**
     * 获取当前值
     *
     * @return 当前值
     */
    @Nullable
    public T get() {
        return value;
    }

    /**
     * 设置新值
     * <p>
     * 只有当新值与旧值不同时才会触发监听器
     *
     * @param newValue 新值
     */
    public void set(@Nullable T newValue) {
        T oldValue = this.value;
        if (!Objects.equals(oldValue, newValue)) {
            this.value = newValue;
            notifyListeners(newValue);
            notifyChangeListeners(oldValue, newValue);
        }
    }

    /**
     * 强制设置新值并触发监听器（即使值相同）
     *
     * @param newValue 新值
     */
    public void forceSet(@Nullable T newValue) {
        T oldValue = this.value;
        this.value = newValue;
        notifyListeners(newValue);
        notifyChangeListeners(oldValue, newValue);
    }

    /**
     * 添加值变化监听器
     *
     * @param listener 监听器，接收新值
     * @return this
     */
    @NotNull
    public ObservableValue<T> onChange(@NotNull Consumer<T> listener) {
        listeners.add(listener);
        return this;
    }

    /**
     * 添加值变化监听器（包含旧值和新值）
     *
     * @param listener 监听器，接收 Change 对象
     * @return this
     */
    @NotNull
    public ObservableValue<T> onChangeWithOld(@NotNull Consumer<Change<T>> listener) {
        changeListeners.add(listener);
        return this;
    }

    /**
     * 移除监听器
     *
     * @param listener 要移除的监听器
     */
    public void removeListener(@NotNull Consumer<T> listener) {
        listeners.remove(listener);
    }

    /**
     * 清除所有监听器
     */
    public void clearListeners() {
        listeners.clear();
        changeListeners.clear();
    }

    /**
     * 检查是否有监听器
     *
     * @return 是否有监听器
     */
    public boolean hasListeners() {
        return !listeners.isEmpty() || !changeListeners.isEmpty();
    }

    private void notifyListeners(T newValue) {
        for (Consumer<T> listener : new ArrayList<>(listeners)) {
            try {
                listener.accept(newValue);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    private void notifyChangeListeners(T oldValue, T newValue) {
        Change<T> change = new Change<>(oldValue, newValue);
        for (Consumer<Change<T>> listener : new ArrayList<>(changeListeners)) {
            try {
                listener.accept(change);
            } catch (Exception e) {
                // 忽略监听器异常
            }
        }
    }

    /**
     * 值变化记录
     *
     * @param <T> 值类型
     */
    public static class Change<T> {
        private final T oldValue;
        private final T newValue;

        public Change(T oldValue, T newValue) {
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public T getOldValue() {
            return oldValue;
        }

        public T getNewValue() {
            return newValue;
        }

        /**
         * 检查是否是从 null 变为非 null
         */
        public boolean isSet() {
            return oldValue == null && newValue != null;
        }

        /**
         * 检查是否是从非 null 变为 null
         */
        public boolean isClear() {
            return oldValue != null && newValue == null;
        }

        /**
         * 检查值是否发生了变化
         */
        public boolean hasChanged() {
            return !Objects.equals(oldValue, newValue);
        }
    }
}