package one.xis.context;


import lombok.Getter;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Getter
abstract class MultiValueParameter implements ComponentParameter {

    private final Class<?> elementType;
    private final int index;
    private final List<?> candidateClasses;

    protected final Collection<Object> values = new HashSet<>();

    MultiValueParameter(Parameter parameter, int index, List<Class<?>> allComponentClasses) {
        this.elementType = findElementType(parameter);
        this.index = index;
        this.candidateClasses = allComponentClasses.stream().filter(this.elementType::isAssignableFrom).collect(Collectors.toList());
    }

    @Override
    public void onComponentCreated(Object o) {
        if (candidateClasses.remove(o.getClass())) { // no subtypes, here
            values.add(o);
        }
    }

    @Override
    public boolean isComplete() {
        return candidateClasses.isEmpty();
    }


    protected abstract Class<?> findElementType(Parameter parameter);
}
