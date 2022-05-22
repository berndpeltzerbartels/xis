package one.xis.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Set;

public interface AppReflection {

    <A extends Annotation> Collection<Field> getFieldsAnnotatedWith(Class<A> anno);

    Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation);
}
