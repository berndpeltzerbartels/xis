package one.xis;

import one.xis.ImportInstances;

/**
 * Application hook for ownership checks.
 *
 * @param <T> type of object protected by the guard
 */
@ImportInstances
public interface OwnershipGuard<T> {

    /**
     * @return {@code true} when the current user may access the submitted object
     */
    boolean mayAccess(T value, UserContext userContext);
}
