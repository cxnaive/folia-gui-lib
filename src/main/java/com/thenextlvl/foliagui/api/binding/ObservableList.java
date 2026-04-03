package com.thenextlvl.foliagui.api.binding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

/**
 * 可观察列表接口
 * <p>
 * 支持列表变化监听和自动更新 GUI 组件
 *
 * @param <E> 元素类型
 * @author TheNextLvl
 */
public interface ObservableList<E> extends List<E> {

    /**
     * 添加列表变化监听器
     * @param listener 监听器
     * @return 此对象
     */
    @NotNull ObservableList<E> onChange(@NotNull Consumer<ListChangeEvent<E>> listener);

    /**
     * 移除列表变化监听器
     * @param listener 监听器
     * @return 此对象
     */
    @NotNull ObservableList<E> removeListener(@NotNull Consumer<ListChangeEvent<E>> listener);

    /**
     * 清除所有监听器
     */
    void clearListeners();

    /**
     * 添加元素并触发事件
     */
    @Override
    boolean add(E element);

    /**
     * 移除元素并触发事件
     */
    @Override
    boolean remove(@Nullable Object element);

    /**
     * 添加所有元素并触发事件
     */
    @Override
    boolean addAll(@NotNull Collection<? extends E> c);

    /**
     * 清空列表并触发事件
     */
    @Override
    void clear();

    /**
     * 设置元素并触发事件
     */
    @Override
    E set(int index, E element);

    /**
     * 在指定位置插入元素并触发事件
     */
    @Override
    void add(int index, E element);

    /**
     * 移除指定位置的元素并触发事件
     */
    @Override
    E remove(int index);

    /**
     * 列表变化事件
     */
    class ListChangeEvent<E> {
        private final ObservableList<E> source;
        private final ChangeType type;
        private final int from;
        private final int to;
        private final List<E> removed;
        private final List<E> added;

        public ListChangeEvent(@NotNull ObservableList<E> source, @NotNull ChangeType type,
                               int from, int to, @NotNull List<E> removed, @NotNull List<E> added) {
            this.source = source;
            this.type = type;
            this.from = from;
            this.to = to;
            this.removed = removed;
            this.added = added;
        }

        public @NotNull ObservableList<E> getSource() {
            return source;
        }

        public @NotNull ChangeType getType() {
            return type;
        }

        public int getFrom() {
            return from;
        }

        public int getTo() {
            return to;
        }

        public @NotNull List<E> getRemoved() {
            return removed;
        }

        public @NotNull List<E> getAdded() {
            return added;
        }

        /**
         * 变化类型
         */
        public enum ChangeType {
            ADD,
            REMOVE,
            SET,
            CLEAR,
            STRUCTURAL
        }
    }
}