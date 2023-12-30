package one.xis;

import java.lang.annotation.Annotation;

public interface Validator<T, A extends Annotation> {

    boolean validate(T value, A annotation);
}
