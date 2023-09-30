package one.xis.context.performance;


import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;

import java.util.Collection;

@XISComponent
@RequiredArgsConstructor
class XC {
    private final Collection<XI> xis;
}
