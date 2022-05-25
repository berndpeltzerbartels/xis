package one.xis.context.simplefield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Comp;

@Comp
@Getter
@RequiredArgsConstructor
class Comp2 {
    private final Comp3 comp3;
}
