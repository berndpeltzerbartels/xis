package one.xis.context.arrayfieldconstructor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

@XISComponent
@Getter
@RequiredArgsConstructor
class ComponentWithArrayField {
    private final Interf1[] field;
}
