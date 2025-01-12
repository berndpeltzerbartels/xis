package one.xis.context2;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

class UnsatisfiedDependencyException extends RuntimeException {
    public UnsatisfiedDependencyException(Collection<Class<?>> unsatisfiedDependencies) {
        super("Unsatisfied dependencies: " + unsatisfiedDependencies);
    }

    public UnsatisfiedDependencyException(Class<?> unsatisfiedDependencies) {
        this(Set.of(unsatisfiedDependencies));
    }

    private static String unsatisfiedDependenciesToString(Collection<Class<?>> unsatisfiedDependencies) {
        return unsatisfiedDependencies.stream().map(Class::getSimpleName).collect(Collectors.joining(", "));
    }
}
