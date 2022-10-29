package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class CollectionUtils {

    public <T> T first(Collection<T> coll) {
        Iterator<T> iterator = coll.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return iterator.next();
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

    public <T> List<T> elementsOfClass(Collection<Object> coll, Class<T> clazz) {
        return coll.stream().filter(clazz::isInstance).map(clazz::cast).collect(Collectors.toList());
    }

    public <T> T findElementOfType(Collection<Object> coll, Class<T> clazz) {
        return coll.stream().filter(clazz::isInstance).map(clazz::cast).collect(CollectorUtils.toOnlyElement());
    }


}
