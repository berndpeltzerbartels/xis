package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@UtilityClass
public class MapperUtil {

    public <S, T> List<T> map(List<S> source, Function<S, T> mapper) {
        return source.stream().map(mapper).collect(Collectors.toList());
    }

}
