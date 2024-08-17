package one.xis.utils.lang;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Predicate;

@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class ObservableSet<T> extends HashSet<T> {

    private final Collection<CollectionListener<T>> collectionListeners = new ConcurrentLinkedDeque<>();

    public void addListener(CollectionListener<T> listener) {
        collectionListeners.add(listener);
    }

    public void removeListener(CollectionListener<T> listener) {
        collectionListeners.remove(listener);
    }

    @Override
    public boolean add(T t) {
        boolean rv = super.add(t);
        elementAdded(t);
        return rv;
    }

    @Override
    public boolean remove(Object o) {
        boolean rv = super.remove(o);
        elementRemoved((T) o);
        return rv;
    }

    @Override
    public void clear() {
        var elements = new HashSet<>(this);
        super.clear();
        elements.forEach(this::elementRemoved);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new AbstractMethodError();
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        throw new AbstractMethodError();
    }

    private void elementAdded(T element) {
        collectionListeners.forEach(collectionListeners -> collectionListeners.onElementAdded(this, element));
    }

    private void elementRemoved(T element) {
        collectionListeners.forEach(collectionListeners -> collectionListeners.onElementRemoved(this, element));
    }
}
