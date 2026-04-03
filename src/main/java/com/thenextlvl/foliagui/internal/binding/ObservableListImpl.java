package com.thenextlvl.foliagui.internal.binding;

import com.thenextlvl.foliagui.api.binding.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

/**
 * ObservableList 实现类
 *
 * @param <E> 元素类型
 * @author TheNextLvl
 */
public class ObservableListImpl<E> extends ArrayList<E> implements ObservableList<E> {

    private final List<Consumer<ListChangeEvent<E>>> listeners = new ArrayList<>();

    public ObservableListImpl() {
        super();
    }

    public ObservableListImpl(@NotNull Collection<? extends E> c) {
        super(c);
    }

    public ObservableListImpl(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public @NotNull ObservableList<E> onChange(@NotNull Consumer<ListChangeEvent<E>> listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public @NotNull ObservableList<E> removeListener(@NotNull Consumer<ListChangeEvent<E>> listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public boolean add(E element) {
        super.add(element);
        fireEvent(ListChangeEvent.ChangeType.ADD, size() - 1, size(), Collections.emptyList(), Collections.singletonList(element));
        return true;
    }

    @Override
    public boolean remove(@Nullable Object element) {
        int index = indexOf(element);
        if (index >= 0) {
            @SuppressWarnings("unchecked")
            E removed = (E) super.remove(index);
            fireEvent(ListChangeEvent.ChangeType.REMOVE, index, index + 1, Collections.singletonList(removed), Collections.emptyList());
            return true;
        }
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        int from = size();
        boolean result = super.addAll(c);
        if (result) {
            fireEvent(ListChangeEvent.ChangeType.ADD, from, size(), Collections.emptyList(), new ArrayList<>(c));
        }
        return result;
    }

    @Override
    public void clear() {
        List<E> removed = new ArrayList<>(this);
        super.clear();
        if (!removed.isEmpty()) {
            fireEvent(ListChangeEvent.ChangeType.CLEAR, 0, 0, removed, Collections.emptyList());
        }
    }

    @Override
    public E set(int index, E element) {
        E oldValue = super.set(index, element);
        fireEvent(ListChangeEvent.ChangeType.SET, index, index + 1, Collections.singletonList(oldValue), Collections.singletonList(element));
        return oldValue;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        fireEvent(ListChangeEvent.ChangeType.ADD, index, index + 1, Collections.emptyList(), Collections.singletonList(element));
    }

    @Override
    public E remove(int index) {
        E removed = super.remove(index);
        fireEvent(ListChangeEvent.ChangeType.REMOVE, index, index + 1, Collections.singletonList(removed), Collections.emptyList());
        return removed;
    }

    /**
     * 触发列表变化事件
     */
    private void fireEvent(ListChangeEvent.ChangeType type, int from, int to,
                           @NotNull List<E> removed, @NotNull List<E> added) {
        ListChangeEvent<E> event = new ListChangeEvent<>(this, type, from, to, removed, added);
        for (Consumer<ListChangeEvent<E>> listener : new ArrayList<>(listeners)) {
            listener.accept(event);
        }
    }

    /**
     * 创建一个空的 ObservableList
     */
    public static <E> ObservableList<E> create() {
        return new ObservableListImpl<>();
    }

    /**
     * 从集合创建 ObservableList
     */
    public static <E> ObservableList<E> of(@NotNull Collection<? extends E> c) {
        return new ObservableListImpl<>(c);
    }
}