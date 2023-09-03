package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
class SingletonInstantiationPostCheck {

    private final SingletonInstantiation singletonInstantiation;

    void check() {
        if (!unusedInstantiators().isEmpty()) {
            Set<Class<?>> allClassesToInstantiate = new HashSet<>(getClassesToInstatiate());
            Set<ConstructorInstantiator> unusedInstantiators = new HashSet<>(unusedInstantiators());
            for (ConstructorInstantiator unusedInstantiator : unusedInstantiators) {
                Set<Class<?>> unsatisfiedConstructorParameterClasses = getUnsatisfiedConstructorParameterClasses(unusedInstantiator).collect(Collectors.toSet());
                unsatisfiedConstructorParameterClasses.removeAll(allClassesToInstantiate);
                if (!unsatisfiedConstructorParameterClasses.isEmpty()) {
                    throw new AppContextException(String.format("unsatisfied dependency in constructor of %s: no singleton(s) of type %s", unusedInstantiator.getType(), kommaSeparatedClassList(unsatisfiedConstructorParameterClasses)));
                }
            }
        }
    }

    private String kommaSeparatedClassList(Collection<Class<?>> classes) {
        return classes.stream().map(Class::getName).collect(Collectors.joining(", "));
    }

    private Collection<ConstructorInstantiator> unusedInstantiators() {
        return singletonInstantiation.getUnusedSingletonInstantiators();// Used ones have been removed
    }

    private Stream<Class<?>> getUnsatisfiedConstructorParameterClasses(ConstructorInstantiator instantiator) {
        return getUnsatisfiedConstructorParameters(instantiator).map(ConstructorParameter::getElementType);
    }

    private Stream<ConstructorParameter> getUnsatisfiedConstructorParameters(ConstructorInstantiator instantiator) {
        return instantiator.getConstructorParameters().stream().filter(constructorParameter -> !constructorParameter.isComplete());
    }

    private Set<Class<?>> getClassesToInstatiate() {
        return singletonInstantiation.getSingletonInstantiators().stream().map(ConstructorInstantiator::getType).collect(Collectors.toSet());
    }
}
