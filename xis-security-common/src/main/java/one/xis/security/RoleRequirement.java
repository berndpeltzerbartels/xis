package one.xis.security;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a hierarchical authentication and role requirement with three levels:
 * - Controller level: an annotation requires authentication; role names additionally require one matching role
 * - Method level: an annotation requires authentication; role names additionally require one matching role
 * - Parameter level: an annotation requires authentication; role names additionally require one matching role
 * 
 * All annotated levels must be satisfied (AND logic between levels).
 * Within each level, any role is sufficient (OR logic within levels).
 */
public class RoleRequirement {
    
    private final boolean controllerAuthenticationRequired;
    private final Set<String> controllerRoles;
    private final boolean methodAuthenticationRequired;
    private final Set<String> methodRoles;
    private final boolean parameterAuthenticationRequired;
    private final Set<String> parameterRoles;
    
    public RoleRequirement(Set<String> controllerRoles, Set<String> methodRoles, Set<String> parameterRoles) {
        this(!isNullOrEmpty(controllerRoles), controllerRoles, !isNullOrEmpty(methodRoles), methodRoles, !isNullOrEmpty(parameterRoles), parameterRoles);
    }

    public RoleRequirement(boolean controllerAuthenticationRequired, Set<String> controllerRoles,
                           boolean methodAuthenticationRequired, Set<String> methodRoles,
                           boolean parameterAuthenticationRequired, Set<String> parameterRoles) {
        this.controllerAuthenticationRequired = controllerAuthenticationRequired;
        this.controllerRoles = controllerRoles != null ? Set.copyOf(controllerRoles) : Collections.emptySet();
        this.methodAuthenticationRequired = methodAuthenticationRequired;
        this.methodRoles = methodRoles != null ? Set.copyOf(methodRoles) : Collections.emptySet();
        this.parameterAuthenticationRequired = parameterAuthenticationRequired;
        this.parameterRoles = parameterRoles != null ? Set.copyOf(parameterRoles) : Collections.emptySet();
    }

    private static boolean isNullOrEmpty(Set<String> roles) {
        return roles == null || roles.isEmpty();
    }
    
    /**
     * Checks if the given user roles satisfy this requirement.
     * 
     * Logic:
     * - If any level was annotated without role names: user must be authenticated
     * - If controllerRoles is non-empty: user must have at least one controller role
     * - AND if methodRoles is non-empty: user must have at least one method role
     * - AND if parameterRoles is non-empty: user must have at least one parameter role
     * 
     * @param userRoles the roles the user possesses; {@code null} means anonymous, an empty set means authenticated
     *                  without named roles
     * @return true if all requirements are satisfied
     */
    public boolean isSatisfiedBy(Set<String> userRoles) {
        return isSatisfiedBy(userRoles, userRoles != null);
    }

    /**
     * Checks if the given authentication state and roles satisfy this requirement.
     * <p>
     * {@code @Authenticated}, or a legacy {@code @Roles} annotation without role names, requires an authenticated user
     * but no specific role. A missing annotation does not create a requirement.
     *
     * @param userRoles the roles the user possesses
     * @param authenticated whether the current user is authenticated
     * @return true if all authentication and role requirements are satisfied
     */
    public boolean isSatisfiedBy(Set<String> userRoles, boolean authenticated) {
        if (hasAuthenticationRequirement() && !authenticated) {
            return false;
        }

        var roles = userRoles != null ? userRoles : Collections.<String>emptySet();
        
        // Check controller level (if required)
        if (!controllerRoles.isEmpty() && hasNoMatchingRole(controllerRoles, roles)) {
            return false;
        }
        
        // Check method level (if required)
        if (!methodRoles.isEmpty() && hasNoMatchingRole(methodRoles, roles)) {
            return false;
        }
        
        // Check parameter level (if required)
        if (!parameterRoles.isEmpty() && hasNoMatchingRole(parameterRoles, roles)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasNoMatchingRole(Set<String> requiredRoles, Set<String> userRoles) {
        return requiredRoles.stream().noneMatch(userRoles::contains);
    }
    
    public boolean hasAnyRequirement() {
        return hasAuthenticationRequirement() || !controllerRoles.isEmpty() || !methodRoles.isEmpty() || !parameterRoles.isEmpty();
    }

    boolean hasAuthenticationRequirement() {
        return controllerAuthenticationRequired || methodAuthenticationRequired || parameterAuthenticationRequired;
    }
    
    public Set<String> getControllerRoles() {
        return controllerRoles;
    }
    
    public Set<String> getMethodRoles() {
        return methodRoles;
    }
    
    public Set<String> getParameterRoles() {
        return parameterRoles;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("RoleRequirement[");
        if (controllerAuthenticationRequired && controllerRoles.isEmpty()) {
            sb.append("controller=(authenticated)");
        }
        if (!controllerRoles.isEmpty()) {
            sb.append("controller=(").append(String.join(" OR ", controllerRoles)).append(")");
        }
        if (methodAuthenticationRequired && methodRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("method=(authenticated)");
        }
        if (!methodRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("method=(").append(String.join(" OR ", methodRoles)).append(")");
        }
        if (parameterAuthenticationRequired && parameterRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("parameter=(authenticated)");
        }
        if (!parameterRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("parameter=(").append(String.join(" OR ", parameterRoles)).append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
