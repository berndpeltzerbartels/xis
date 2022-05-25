package one.xis.context.arrayfield;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Comp;
import one.xis.context.Inj;

@Comp
@Getter
@RequiredArgsConstructor
class Comp1 {
    private final Interf1[] field1;

    @Inj
    private final Interf1[] field2;
}
