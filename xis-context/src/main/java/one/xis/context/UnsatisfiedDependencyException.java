package one.xis.context;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

class UnsatisfiedDependencyException extends RuntimeException {

    UnsatisfiedDependencyException(Field field) {
        super("unsatisfied dependency for " + field);
    }

    UnsatisfiedDependencyException(Parameter field) {
        super("unsatisfied dependency for " + field);
    }

}
