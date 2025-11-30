package one.xis.security;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a hierarchical role requirement with three levels:
 * - Controller level: At least one role from this set must be present
 * - Method level: At least one role from this set must be present (if non-empty)
 * - Parameter level: At least one role from this set must be present (if non-empty)
 * 
 * All non-empty levels must be satisfied (AND logic between levels).
 * Within each level, any role is sufficient (OR logic within levels).
 */
public class RoleRequirement {
    
    private final Set<String> controllerRoles;
    private final Set<String> methodRoles;
    private final Set<String> parameterRoles;
    
    public RoleRequirement(Set<String> controllerRoles, Set<String> methodRoles, Set<String> parameterRoles) {
        this.controllerRoles = controllerRoles != null ? Set.copyOf(controllerRoles) : Collections.emptySet();
        this.methodRoles = methodRoles != null ? Set.copyOf(methodRoles) : Collections.emptySet();
        this.parameterRoles = parameterRoles != null ? Set.copyOf(parameterRoles) : Collections.emptySet();
    }
    
    /**
     * Checks if the given user roles satisfy this requirement.
     * 
     * Logic:
     * - If controllerRoles is non-empty: user must have at least one controller role
     * - AND if methodRoles is non-empty: user must have at least one method role
     * - AND if parameterRoles is non-empty: user must have at least one parameter role
     * 
     * @param userRoles the roles the user possesses
     * @return true if all non-empty requirement levels are satisfied
     */
    public boolean isSatisfiedBy(Set<String> userRoles) {
        if (userRoles == null || userRoles.isEmpty()) {
            // No roles means only accessible if no roles are required
            return controllerRoles.isEmpty() && methodRoles.isEmpty() && parameterRoles.isEmpty();
        }
        
        // Check controller level (if required)
        if (!controllerRoles.isEmpty() && hasNoMatchingRole(controllerRoles, userRoles)) {
            return false;
        }
        
        // Check method level (if required)
        if (!methodRoles.isEmpty() && hasNoMatchingRole(methodRoles, userRoles)) {
            return false;
        }
        
        // Check parameter level (if required)
        if (!parameterRoles.isEmpty() && hasNoMatchingRole(parameterRoles, userRoles)) {
            return false;
        }
        
        return true;
    }
    
    private boolean hasNoMatchingRole(Set<String> requiredRoles, Set<String> userRoles) {
        return requiredRoles.stream().noneMatch(userRoles::contains);
    }
    
    public boolean hasAnyRequirement() {
        return !controllerRoles.isEmpty() || !methodRoles.isEmpty() || !parameterRoles.isEmpty();
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
        if (!controllerRoles.isEmpty()) {
            sb.append("controller=(").append(String.join(" OR ", controllerRoles)).append(")");
        }
        if (!methodRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("method=(").append(String.join(" OR ", methodRoles)).append(")");
        }
        if (!parameterRoles.isEmpty()) {
            if (sb.length() > "RoleRequirement[".length()) sb.append(" AND ");
            sb.append("parameter=(").append(String.join(" OR ", parameterRoles)).append(")");
        }
        sb.append("]");
        return sb.toString();
    }
}
