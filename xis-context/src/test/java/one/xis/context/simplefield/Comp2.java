package one.xis.context.simplefield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@Getter
@RequiredArgsConstructor
class Comp2 {
    private final Comp3 comp3;
}
