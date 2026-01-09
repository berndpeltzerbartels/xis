package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;

@UtilityClass
public class CollectionUtils {


    public <T> void resize(Collection<T> collection, int size) {
        while (collection.size() < size) {
            collection.add(null);
        }
    }

    public <T> T first(Collection<T> coll) {
        Iterator<T> iterator = coll.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return iterator.next();
    }

    public <T> T removeOne(Collection<T> coll) {
        var first = first(coll);
        coll.remove(first);
        return first;
    }

    public <T> T onlyElement(Collection<T> coll) {
        if (coll.size() != 1) {
            throw new IllegalStateException(coll.size() + " elements instead of one");
        }
        return first(coll);
    }

    public <T> T onlyElement(Collection<T> coll, Supplier<RuntimeException> exceptionSupplier) {
        if (coll.size() != 1) {
            throw exceptionSupplier.get();
        }
        return first(coll);
    }

    @SuppressWarnings("unchecked")
    public <C extends Collection<?>> C convertCollectionClass(Collection<Object> coll, Class<C> clazz) {
        if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            var constructor = ClassUtils.getConstructor(clazz, Collection.class);
            if (constructor != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
                try {
                    return constructor.newInstance(coll);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (clazz.isAssignableFrom(List.class)) {
            return (C) new ArrayList<>(coll);
        }
        if (clazz.isAssignableFrom(Set.class)) {
            return (C) new HashSet<>(coll);
        }
        if (clazz.isAssignableFrom(HashSet.class)) {
            return (C) new HashSet<>(coll);
        }
        if (clazz.isAssignableFrom(SortedSet.class)) {
            return (C) new TreeSet<>(coll);
        }
        throw new UnsupportedOperationException("unable to instantiate " + clazz);
    }

    public <T> T findElementOfType(Collection<Object> coll, Class<T> clazz) {
        return coll.stream().filter(clazz::isInstance).map(clazz::cast).collect(CollectorUtils.toOnlyElement());
    }

    @SuppressWarnings("unchecked")
    public <C extends Collection<?>> C emptyInstance(Class<C> clazz) {
        if (clazz.isAssignableFrom(List.class)) {
            return (C) new ArrayList<>();
        }
        if (clazz.isAssignableFrom(Set.class)) {
            return (C) new HashSet<>();
        }
        if (clazz.isAssignableFrom(HashSet.class)) {
            return (C) new HashSet<>();
        }
        var constructor = ClassUtils.getConstructor(clazz);
        if (constructor != null && !Modifier.isAbstract(clazz.getModifiers()) && !Modifier.isInterface(clazz.getModifiers())) {
            try {
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new UnsupportedOperationException("unable to instantiate " + clazz);
    }
}
