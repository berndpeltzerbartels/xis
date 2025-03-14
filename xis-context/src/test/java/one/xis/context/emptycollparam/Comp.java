package one.xis.context.emptycollparam;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class Comp {

    @Getter
    private final Collection<Interf> components;
}
