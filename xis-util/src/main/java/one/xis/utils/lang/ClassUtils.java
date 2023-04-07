package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
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
            throw new RuntimeException(aClass + " must habe a default contructor");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Constructor<T> getConstructor(Class<T> aClass, Class<?> parameterTypes) {
        try {
            var constructor = aClass.getConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            return null;
        }
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

    public static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
