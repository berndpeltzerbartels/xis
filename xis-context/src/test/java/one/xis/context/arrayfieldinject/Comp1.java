package one.xis.context.arrayfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Inject;

@Component
@Getter
@RequiredArgsConstructor
class Comp1 {
    @Inject
    private Interf1[] field;
}
