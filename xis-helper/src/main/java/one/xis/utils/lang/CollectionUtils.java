package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

@UtilityClass
public class CollectionUtils {

    public <T> T first(Collection<T> coll) {
        Iterator<T> iterator = coll.iterator();
        if (!iterator.hasNext()) {
            throw new NoSuchElementException();
        }
        return iterator.next();
    }

}
