package one.xis.context;

import java.util.Collection;
import java.util.stream.Collectors;

class UnsatisfiedDependencyException extends RuntimeException {
    public UnsatisfiedDependencyException(Collection<Class<?>> unsatisfiedDependencies) {
        super("Unsatisfied dependencies: " + unsatisfiedDependencies);
    }

    public UnsatisfiedDependencyException(Class<?> unsatisfiedDependencies, SingletonConsumer consumer) {
        super("Unsatisfied dependencies: " + unsatisfiedDependencies + " in " + consumer);
    }

    private static String unsatisfiedDependenciesToString(Collection<Class<?>> unsatisfiedDependencies) {
        return unsatisfiedDependencies.stream().map(Class::getSimpleName).collect(Collectors.joining(", "));
    }
}
