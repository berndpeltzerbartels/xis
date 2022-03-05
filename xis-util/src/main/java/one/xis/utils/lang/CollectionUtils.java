package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

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

}
