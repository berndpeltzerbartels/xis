package one.xis.i18n;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class Mappers {
    private final Collection<Mapper<?>> mappers;

    public Mapper<?> getMapper(Class<?> type) {
        return mappers.stream()
                .filter(m -> m.getType().isAssignableFrom(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("no mapper found for type " + type));
    }

}
