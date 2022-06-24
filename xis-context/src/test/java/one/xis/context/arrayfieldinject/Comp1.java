package one.xis.context.arrayfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Comp;
import one.xis.context.Inj;

@Comp
@Getter
@RequiredArgsConstructor
class Comp1 {
    @Inj
    private Interf1[] field;
}
