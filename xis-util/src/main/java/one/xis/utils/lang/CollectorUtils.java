package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class CollectorUtils {
    public <E> Collector<E, ?, E> toOnlyElement() {
        return toOnlyElement(list -> new IllegalArgumentException(list.size() + " elements instead of exactly one"));
    }

    public <E> Collector<E, ?, E> toOnlyElement(Function<List, RuntimeException> exeptionMapper) {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list -> {
            if (list.size() != 1) {
                throw exeptionMapper.apply(list);// IllegalArgumentException(list.size() + " elements instead of exactly one");
            }
            return list.get(0);
        });
    }

    public <E> Collector<E, ?, Optional<E>> toOnlyOptional(Function<List, RuntimeException> exeptionMapper) {
        return Collectors.collectingAndThen(Collectors.toCollection(ArrayList::new), list -> {
            switch (list.size()) {
                case 0:
                    return Optional.empty();
                case 1:
                    return Optional.of(list.get(0));
                default:
                    throw exeptionMapper.apply(list);
            }
        });
    }
}
