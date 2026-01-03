package one.xis.context.arrayfieldinject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.XISInject;

@Component
@Getter
@RequiredArgsConstructor
class Comp1 {
    @XISInject
    private Interf1[] field;
}
