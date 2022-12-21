package one.xis.context;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Set;

class NoopReflection implements Reflection {

    @Override
    public Set<Field> getDependencyFields() {
        return Collections.emptySet();
    }

    @Override
    public Set<Class<?>> getComponentTypes() {
        return Collections.emptySet();
    }
}
