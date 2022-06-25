package one.xis.context.arrayfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInject;

@XISComponent
@Getter
@RequiredArgsConstructor
class Comp1 {
    @XISInject
    private Interf1[] field;
}
