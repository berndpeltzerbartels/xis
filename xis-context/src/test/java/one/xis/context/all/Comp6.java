package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Getter
@Component
@RequiredArgsConstructor
class Comp6 {
    private final Comp5 comp5;
}
