package one.xis.context.interfacewithdefault;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.List;

@Getter
@Component
@RequiredArgsConstructor
class FieldHolder {
    private final List<Interf> fieldByConstructor;

    @Inject
    private List<Interf> injectedField;

}
