package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

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
}
