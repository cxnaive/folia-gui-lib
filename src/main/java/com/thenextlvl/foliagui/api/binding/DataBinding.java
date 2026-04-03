package com.thenextlvl.foliagui.api.binding;

import com.thenextlvl.foliagui.api.component.Component;
import com.thenextlvl.foliagui.internal.binding.ObservableListImpl;
import com.thenextlvl.foliagui.internal.binding.ObservableValueImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 数据绑定工具类
 * <p>
 * 提供便捷的方法创建和绑定 Observable 值
 *
 * @author TheNextLvl
 */
public final class DataBinding {

    private DataBinding() {}

    // ==================== 创建 ObservableValue ====================

    /**
     * 创建一个空的 ObservableValue
     */
    @NotNull
    public static <T> ObservableValue<T> observable() {
        return new ObservableValueImpl<>();
    }

    /**
     * 创建一个带初始值的 ObservableValue
     */
    @NotNull
    public static <T> ObservableValue<T> observable(@Nullable T initialValue) {
        return new ObservableValueImpl<>(initialValue);
    }

    /**
     * 创建一个绑定到 Supplier 的 ObservableValue
     */
    @NotNull
    public static <T> ObservableValue<T> observable(@NotNull Supplier<T> supplier) {
        ObservableValueImpl<T> observable = new ObservableValueImpl<>(supplier.get());
        observable.bindTo(supplier);
        return observable;
    }

    // ==================== 创建 ObservableList ====================

    /**
     * 创建一个空的 ObservableList
     */
    @NotNull
    public static <E> ObservableList<E> observableList() {
        return new ObservableListImpl<>();
    }

    /**
     * 创建一个带初始元素的 ObservableList
     */
    @NotNull
    public static <E> ObservableList<E> observableList(@NotNull Collection<? extends E> elements) {
        return new ObservableListImpl<>(elements);
    }

    // ==================== 绑定到组件 ====================

    /**
     * 将 ObservableValue 绑定到组件更新
     * @param observable 可观察值
     * @param onUpdate 值变化时的更新回调
     * @return 可观察值
     */
    @NotNull
    public static <T> ObservableValue<T> bindToUpdate(@NotNull ObservableValue<T> observable,
                                                       @NotNull Runnable onUpdate) {
        observable.onChange(event -> onUpdate.run());
        return observable;
    }

    /**
     * 将 ObservableValue 绑定到组件显示
     * @param observable 可观察值
     * @param component 目标组件
     * @param renderer 值到显示物品的渲染器
     * @return 可观察值
     */
    @NotNull
    public static <T> ObservableValue<T> bindToDisplay(@NotNull ObservableValue<T> observable,
                                                        @NotNull Component component,
                                                        @NotNull Function<T, org.bukkit.inventory.ItemStack> renderer) {
        observable.onChange(event -> {
            T value = event.getNewValue();
            org.bukkit.inventory.ItemStack item = renderer.apply(value);
            component.setDisplayItem(item);
        });
        return observable;
    }

    // ==================== 计算属性 ====================

    /**
     * 创建一个基于 ObservableValue 的计算属性
     * @param source 源 ObservableValue
     * @param calculator 计算函数
     * @return 计算结果的 ObservableValue
     */
    @NotNull
    public static <T, R> ObservableValue<R> compute(@NotNull ObservableValue<T> source,
                                                     @NotNull Function<T, R> calculator) {
        ObservableValueImpl<R> result = new ObservableValueImpl<>(calculator.apply(source.get()));
        source.onChange(event -> result.set(calculator.apply(event.getNewValue())));
        return result;
    }

    /**
     * 双向绑定两个 ObservableValue
     */
    public static <T> void bindBidirectional(@NotNull ObservableValue<T> a, @NotNull ObservableValue<T> b) {
        a.bindBidirectional(b);
    }

    // ==================== 类型转换 ====================

    /**
     * 将 ObservableValue<String> 转换为 Integer
     */
    @NotNull
    public static ObservableValue<Integer> asInteger(@NotNull ObservableValue<String> observable) {
        return observable.map(s -> {
            try {
                return s != null ? Integer.parseInt(s) : 0;
            } catch (NumberFormatException e) {
                return 0;
            }
        });
    }

    /**
     * 将 ObservableValue<String> 转换为 Double
     */
    @NotNull
    public static ObservableValue<Double> asDouble(@NotNull ObservableValue<String> observable) {
        return observable.map(s -> {
            try {
                return s != null ? Double.parseDouble(s) : 0.0;
            } catch (NumberFormatException e) {
                return 0.0;
            }
        });
    }

    /**
     * 将 ObservableValue<String> 转换为 Boolean
     */
    @NotNull
    public static ObservableValue<Boolean> asBoolean(@NotNull ObservableValue<String> observable) {
        return observable.map(s -> "true".equalsIgnoreCase(s) || "1".equals(s) || "yes".equalsIgnoreCase(s));
    }
}