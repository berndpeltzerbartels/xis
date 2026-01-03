package one.xis.context.arrayfieldconstructor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@Getter
@RequiredArgsConstructor
class ComponentWithArrayField {
    private final Interf1[] field;
}
