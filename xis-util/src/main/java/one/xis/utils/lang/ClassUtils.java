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
            return newInstanceWithDefaults(aClass);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T newInstanceWithDefaults(Class<T> aClass) {
        Constructor<?>[] constructors = aClass.getDeclaredConstructors();
        Arrays.sort(constructors, Comparator.comparingInt(Constructor::getParameterCount));

        for (Constructor<?> constructor : constructors) {
            try {
                constructor.setAccessible(true);
                Object[] parameters = createDefaultParameters(constructor);
                @SuppressWarnings("unchecked")
                T instance = (T) constructor.newInstance(parameters);
                return instance;
            } catch (Exception e) {
                // Try next constructor
            }
        }
        throw new RuntimeException("Could not instantiate class " + aClass.getName());
    }

    private static Object[] createDefaultParameters(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] parameters = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            parameters[i] = getDefaultValue(parameterTypes[i]);
        }

        return parameters;
    }

    private static Object getDefaultValue(Class<?> type) {
        // Primitives
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0.0f;
        if (type == double.class) return 0.0;
        if (type == char.class) return '\u0000';

        // Common types
        if (type == String.class) return "";
        if (type == Boolean.class) return Boolean.FALSE;
        if (type == Integer.class) return 0;
        if (type == Long.class) return 0L;
        if (type == Double.class) return 0.0;
        if (type == Float.class) return 0.0f;
        if (type == Short.class) return (short) 0;
        if (type == Byte.class) return (byte) 0;
        if (type == Character.class) return '\u0000';

        // Collections
        if (Collection.class.isAssignableFrom(type)) {
            @SuppressWarnings("unchecked")
            Class<? extends Collection<?>> collectionType = (Class<? extends Collection<?>>) type;
            return CollectionUtils.emptyInstance(collectionType);
        }

        if (Map.class.isAssignableFrom(type)) {
            return defaultMap(type);
        }

        // Arrays
        if (type.isArray()) {
            return java.lang.reflect.Array.newInstance(type.getComponentType(), 0);
        }

        // Enums
        if (type.isEnum()) {
            Object[] enumConstants = type.getEnumConstants();
            return enumConstants.length > 0 ? enumConstants[0] : null;
        }

        // Records
        if (type.isRecord()) {
            return createRecordInstance(type);
        }

        // Complex types - recursively instantiate
        try {
            return newInstance(type);
        } catch (Exception e) {
            return null;
        }
    }

    private static Map<?, ?> defaultMap(Class<?> type) {
        if (type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
            return new HashMap<>();
        }
        try {
            Constructor<?> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return (Map<?, ?>) constructor.newInstance();
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
    
    private static <T> T createRecordInstance(Class<T> recordClass) {
        try {
            // Get canonical constructor for record
            var recordComponents = recordClass.getRecordComponents();
            Class<?>[] parameterTypes = Arrays.stream(recordComponents)
                    .map(java.lang.reflect.RecordComponent::getType)
                    .toArray(Class<?>[]::new);

            Constructor<T> constructor = recordClass.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);

            Object[] parameters = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = getDefaultValue(parameterTypes[i]);
            }

            return constructor.newInstance(parameters);
        } catch (Exception e) {
            throw new RuntimeException("Could not instantiate record " + recordClass.getName(), e);
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
