package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class CollectorUtils {
    public <E> Collector<E, ?, E> onlyElement() {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list -> {
            if (list.size() != 1) {
                throw new IllegalArgumentException(list.size() + " elements instead of exactly one");
            }
            return list.get(0);
        });
    }
}
