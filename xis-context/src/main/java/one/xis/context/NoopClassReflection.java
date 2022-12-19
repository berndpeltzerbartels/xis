package one.xis.context;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

class NoopClassReflection implements ClassReflection {
    @Override
    public <A extends Annotation> Collection<Field> getFieldsAnnotatedWith(Class<A> anno) {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation> annotation) {
        return Collections.emptySet();
    }
}
