package one.xis.security;

import one.xis.Authenticated;
import one.xis.Roles;
import one.xis.UserContext;
import one.xis.auth.AuthenticationException;
import one.xis.auth.AuthorizationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;


public class SecurityUtil {

    public static String createRandomKey(int length) {
        byte[] keyBytes = new byte[length];
        new SecureRandom().nextBytes(keyBytes);
        return Base64.getEncoder().withoutPadding().encodeToString(keyBytes);
    }

    public static String signHmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC", e);
        }
    }

    public static String encodeBase64UrlSafe(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] decodeBase64UrlSafe(String value) {
        int paddingNeeded = (4 - (value.length() % 4)) % 4;
        value += "=".repeat(paddingNeeded);
        try {
            return Base64.getUrlDecoder().decode(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid Base64 URL safe string", e);
        }
    }

    public static void checkRoles(Method method, UserContext userContext) {
        checkRoles(method, roles(userContext), authenticated(userContext));
    }

    private static void checkRoles(Method method, Set<String> presentRoles, boolean authenticated) {
        var roleRequirement = getRoleRequirement(method);
        if (!roleRequirement.hasAnyRequirement()) {
            return;
        }
        if (!roleRequirement.isSatisfiedBy(presentRoles, authenticated)) {
            throwExceptionForUnsatisfiedRequirement(authenticated, "method: " + method.getName(), roleRequirement);
        }
    }

    public static void checkRoles(Class<?> c, UserContext userContext) {
        checkRoles(c, roles(userContext), authenticated(userContext));
    }

    private static void checkRoles(Class<?> c, Set<String> presentRoles, boolean authenticated) {
        var controllerRoles = getControllerRoles(c);
        var roleRequirement = new RoleRequirement(hasControllerAuthenticationAnnotation(c), controllerRoles, false, Collections.emptySet(), false, Collections.emptySet());
        if (!roleRequirement.hasAnyRequirement()) {
            return;
        }
        if (!roleRequirement.isSatisfiedBy(presentRoles, authenticated)) {
            throwExceptionForUnsatisfiedRequirement(authenticated, "class: " + c.getName(), roleRequirement);
        }
    }

    private static void throwExceptionForUnsatisfiedRequirement(boolean authenticated, String target, RoleRequirement roleRequirement) {
        if (!authenticated && roleRequirement.hasAuthenticationRequirement()) {
            throw new AuthenticationException("User is not authenticated for " + target + ". Required: " + roleRequirement);
        }
        throw new AuthorizationException("User does not have required roles for " + target + ". Required: " + roleRequirement);
    }

    public static RoleRequirement getRoleRequirement(Method method) {
        Set<String> controllerRoles = getControllerRoles(method.getDeclaringClass());
        Set<String> methodRoles = getMethodRoles(method);
        Set<String> parameterRoles = getParameterRoles(method);

        return new RoleRequirement(
                hasControllerAuthenticationAnnotation(method.getDeclaringClass()), controllerRoles,
                hasMethodAuthenticationAnnotation(method), methodRoles,
                hasParameterAuthenticationAnnotation(method), parameterRoles);
    }

    private static Set<String> roles(UserContext userContext) {
        if (userContext == null || userContext.getRoles() == null) {
            return Collections.emptySet();
        }
        return userContext.getRoles();
    }

    private static boolean authenticated(UserContext userContext) {
        return userContext != null && userContext.isAuthenticated();
    }

    private static boolean hasControllerAuthenticationAnnotation(Class<?> c) {
        while (c != null && c != Object.class) {
            if (c.isAnnotationPresent(Roles.class) || c.isAnnotationPresent(Authenticated.class)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return false;
    }

    private static Set<String> getControllerRoles(Class<?> c) {
        var roles = new HashSet<String>();
        while (c != null && c != Object.class) {
            if (c.isAnnotationPresent(Roles.class)) {
                roles.addAll(Arrays.asList(c.getAnnotation(Roles.class).value()));
            }
            c = c.getSuperclass();
        }
        return roles;
    }

    private static Set<String> getMethodRoles(Method method) {
        if (method.isAnnotationPresent(Roles.class)) {
            return new HashSet<>(Arrays.asList(method.getAnnotation(Roles.class).value()));
        }
        return Collections.emptySet();
    }

    private static Set<String> getParameterRoles(Method method) {
        var roles = new HashSet<String>();
        for (Parameter parameter : method.getParameters()) {
            Class<?> parameterType = parameter.getType();
            while (parameterType != null && parameterType != Object.class) {
                if (parameterType.isAnnotationPresent(Roles.class)) {
                    roles.addAll(Arrays.asList(parameterType.getAnnotation(Roles.class).value()));
                }
                parameterType = parameterType.getSuperclass();
            }
        }
        return roles;
    }

    private static boolean hasMethodAuthenticationAnnotation(Method method) {
        return method.isAnnotationPresent(Roles.class) || method.isAnnotationPresent(Authenticated.class);
    }

    private static boolean hasParameterAuthenticationAnnotation(Method method) {
        for (Parameter parameter : method.getParameters()) {
            Class<?> parameterType = parameter.getType();
            if (parameter.isAnnotationPresent(Authenticated.class)) {
                return true;
            }
            while (parameterType != null && parameterType != Object.class) {
                if (parameterType.isAnnotationPresent(Roles.class) || parameterType.isAnnotationPresent(Authenticated.class)) {
                    return true;
                }
                parameterType = parameterType.getSuperclass();
            }
        }
        return false;
    }

    @Deprecated
    public static Set<String> getRequiredRoles(Method method) {
        var roleRequirement = getRoleRequirement(method);
        var allRoles = new HashSet<String>();
        allRoles.addAll(roleRequirement.getControllerRoles());
        allRoles.addAll(roleRequirement.getMethodRoles());
        allRoles.addAll(roleRequirement.getParameterRoles());
        return allRoles;
    }

    @Deprecated
    public static Set<String> getRequiredRoles(Class<?> c) {
        return getControllerRoles(c);
    }
}
