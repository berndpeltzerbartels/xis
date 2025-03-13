package one.xis.utils.lang;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@UtilityClass
public class ClassUtils {

    @SuppressWarnings("unchecked")
    public <T, R extends T> R cast(T obj, Class<R> target, Supplier<RuntimeException> exceptionSupplier) {
        if (!target.isInstance(obj)) {
            throw exceptionSupplier.get();
        }
        return (R) obj;
    }

    public <T, R extends T> R cast(T obj, Class<R> target) {
        return cast(obj, target, () -> new ClassCastException(obj.getClass() + " can not be casted to " + target));
    }

    public <T> T newInstance(Class<T> aClass) {
        try {
            Constructor<T> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(aClass + " must have a default contructor");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean areRelated(Class<?> c1, Class<?> c2) {
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }

    public boolean isComplex(Class<?> c) {
        return !c.isPrimitive() && !c.isEnum() && !c.isArray();
    }

    public boolean isCustomType(Class<?> c) {
        if (c.isPrimitive() || c.isEnum() || c.isArray()) {
            return false;
        }
        return !c.getName().startsWith("java.lang");
    }

    public Constructor<?> getUniqueConstructor(Class<?> c) {
        if (c.getDeclaredConstructors().length > 1) {
            throw new RuntimeException("Multiple constructors found for class " + c.getName());
        }
        if (c.getDeclaredConstructors().length == 0) {
            throw new RuntimeException("No constructor found for class " + c.getName());
        }
        return c.getDeclaredConstructors()[0];
    }

    public <T> Constructor<T> getConstructor(Class<T> aClass, Class<?>... parameterTypes) {
        try {
            var constructor = aClass.getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
    }


    @SuppressWarnings("unchecked")
    public static <T> Optional<Constructor<T>> getAccessibleConstructor(Class<T> aClass) {
        return Arrays.stream(aClass.getDeclaredConstructors())
                .map(c -> (Constructor<T>) c)
                .filter(ClassUtils::nonPrivate)
                .findFirst();

    }

    public Class<?> lastDescendant(Set<Class<?>> relatedClasses) {
        LinkedList<Class<?>> sorted = relatedClasses.stream()
                .sorted(Comparator.comparing(ClassUtils::inheritanceLevel).reversed())
                .collect(Collectors.toCollection(LinkedList::new));
        Set<Class<?>> cloned = new HashSet<>(sorted);
        Class<?> c = sorted.getFirst();
        while (c != null) {
            cloned.remove(c);
            c = c.getSuperclass();
        }
        if (!cloned.isEmpty()) {
            throw new IllegalStateException("not related: " + relatedClasses);
        }

        return sorted.getFirst();
    }

    private int inheritanceLevel(Class<?> clazz) {
        int level = 0;
        Class<?> c = clazz;
        while (!c.equals(Object.class)) {
            level++;
            c = c.getSuperclass();
        }
        return level;
    }

    public static boolean related(Class<?> c1, Class<?> c2) {
        return c1.isAssignableFrom(c2) || c2.isAssignableFrom(c1);
    }

    public static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean nonPrivate(Executable executable) {
        return !Modifier.isPrivate(executable.getModifiers());
    }

    @NonNull
    public static Class<?> getGenericInterfacesTypeParameter(@NonNull Class<?> c, @NonNull Class<?> interf, int index) {
        if (!interf.isInterface()) {
            throw new IllegalArgumentException(interf + " is not an interface");
        }
        for (var inter : c.getGenericInterfaces()) {
            if (inter instanceof ParameterizedType parameterizedType) {
                if (parameterizedType.getRawType().equals(interf)) {
                    var typeParameter = parameterizedType.getActualTypeArguments()[index];
                    if (typeParameter instanceof Class<?> clazz) {
                        return clazz;
                    }
                    if (typeParameter instanceof ParameterizedType parameterizedType1) {
                        return (Class<?>) parameterizedType1.getRawType();
                    }
                    throw new IllegalArgumentException("unsupported type parameter in " + c + " for " + interf + " at index " + index);
                }
            }
        }
        throw new IllegalArgumentException(interf + " is not implemented by " + c);
    }

    public boolean isPrimitiveWrapper(Class<?> type) {
        return type == Integer.class || type == Long.class || type == Short.class || type == Byte.class || type == Float.class || type == Double.class || type == Character.class;
    }

    public boolean isNumber(Class<?> type) {
        return Number.class.isAssignableFrom(type) || type.isPrimitive() && type != boolean.class && type != char.class;
    }

    public boolean isDate(@NonNull Class<?> type) {
        return Date.class.isAssignableFrom(type) || java.sql.Date.class.isAssignableFrom(type) || java.sql.Timestamp.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type);
    }

    public boolean isBoolean(Class<?> type) {
        return type == boolean.class || type == Boolean.class;
    }

    public boolean isString(Class<?> type) {
        return type == String.class;
    }

    public boolean isEnum(Class<?> type) {
        return type.isEnum();
    }

    public boolean isAnnotationPresentInHierarchy(Class<?> clazz, Class<? extends Annotation> annotation) {
        if (clazz.isAnnotationPresent(annotation)) {
            return true;
        }
        Class<?> c = clazz;
        while (c != null && !c.equals(Object.class)) {
            if (c.isAnnotationPresent(annotation)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }


}
