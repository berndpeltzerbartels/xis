package one.xis.context.performance;


import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class XC {
    private final Collection<XI> xis;
}
