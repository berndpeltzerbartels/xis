package one.xis.deserialize;

import one.xis.OwnedBy;
import one.xis.OwnershipGuard;
import one.xis.UserContext;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class OwnershipCheck {

    private final List<OwnershipGuard<?>> guards;

    OwnershipCheck(List<OwnershipGuard<?>> guards) {
        this.guards = guards;
    }

    void checkObject(String path, AnnotatedElement target, Object value, UserContext userContext, PostProcessingResults results) {
        if (value == null) {
            return;
        }
        var guardClasses = guardClasses(target, value.getClass());
        if (guardClasses.isEmpty()) {
            return;
        }
        if (userContext == null || !userContext.isAuthenticated() || userContext.getUserId() == null) {
            results.add(new AccessDeniedError(context(path, target, userContext), "Ownership check requires an authenticated user", true));
            return;
        }

        for (var guardClass : guardClasses) {
            if (!mayAccess(guardClass, value, userContext)) {
                results.add(new AccessDeniedError(context(path, target, userContext), "Current user may not access " + value.getClass().getName(), false));
                return;
            }
        }
    }

    private Set<Class<? extends OwnershipGuard<?>>> guardClasses(AnnotatedElement target, Class<?> valueType) {
        Set<Class<? extends OwnershipGuard<?>>> result = new LinkedHashSet<>();
        if (target.isAnnotationPresent(OwnedBy.class)) {
            result.add(target.getAnnotation(OwnedBy.class).value());
        }
        if (valueType.isAnnotationPresent(OwnedBy.class)) {
            result.add(valueType.getAnnotation(OwnedBy.class).value());
        }
        return result;
    }

    private DeserializationContext context(String path, AnnotatedElement target, UserContext userContext) {
        return new DeserializationContext(path, target, OwnedBy.class, userContext);
    }

    @SuppressWarnings("unchecked")
    private <T> boolean mayAccess(Class<? extends OwnershipGuard<?>> guardClass, Object value, UserContext userContext) {
        var guard = (OwnershipGuard<T>) guards.stream()
                .filter(guardClass::isInstance)
                .findFirst()
                .orElseGet(() -> ClassUtils.newInstance(guardClass));
        return guard.mayAccess((T) value, userContext);
    }
}
