package one.xis.context.emptycollparam;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
class Comp {

    @Getter
    private final Collection<Interf> components;
}
