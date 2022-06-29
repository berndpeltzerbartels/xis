package one.xis.context;


import lombok.Getter;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
abstract class MultiValueParameter extends ConstructorParameter {

    private final Class<?> elementType;
    private final Collection<Object> values = new HashSet<>();
    private Set<?> candidateClasses;

    MultiValueParameter(Parameter parameter) {
        this.elementType = findElementType(parameter);
    }

    void registerSingletonClasses(Collection<Class<?>> allSingeltonClasses) {
        candidateClasses = allSingeltonClasses.stream().filter(elementType::isAssignableFrom).collect(Collectors.toSet());
    }

    @Override
    public void onComponentCreated(Object o) {
        if (candidateClasses.remove(o.getClass())) { // no subtypes, here
            values.add(o);
        }
    }

    @Override
    boolean isComplete() {
        return candidateClasses.isEmpty();
    }


    protected abstract Class<?> findElementType(Parameter parameter);
}
