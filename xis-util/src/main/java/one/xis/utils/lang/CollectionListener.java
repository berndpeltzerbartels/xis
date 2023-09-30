package one.xis.utils.lang;

import java.util.Set;

public interface CollectionListener<T> {

    default void onElementAdded(Set<T> source, T element) {
    }

    default void onElementRemoved(Set<T> source, T element) {
    }
}
