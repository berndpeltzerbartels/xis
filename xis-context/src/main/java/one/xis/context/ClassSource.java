package one.xis.context;

import java.lang.reflect.Field;
import java.util.Set;

public interface ClassSource {

    Set<Field> getDependencyFields();

    Set<Class<?>> getComponentTypes();

}
