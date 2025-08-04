package one.xis.security;

import one.xis.Roles;
import one.xis.auth.AuthenticationException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    public static void checkRoles(Method method, Set<String> presentRoles) {
        var requiredRoles = getRequiredRoles(method);
        if (requiredRoles.isEmpty()) {
            return;
        }
        // check if user has at least one of the required roles
        if (presentRoles == null || presentRoles.isEmpty() || requiredRoles.stream().noneMatch(presentRoles::contains)) {
            throw new AuthenticationException("User does not have required roles for method: " + method.getName());
        }
    }

    public static void checkRoles(Class<?> c, Set<String> presentRoles) {
        var requiredRoles = getRequiredRoles(c);
        if (requiredRoles.isEmpty()) {
            return;
        }
        // check if user has at least one of the required roles
        if (presentRoles == null || presentRoles.isEmpty() || requiredRoles.stream().noneMatch(presentRoles::contains)) {
            throw new AuthenticationException("User does not have required roles for class: " + c.getName());
        }
    }

    public static Set<String> getRequiredRoles(Method method) {
        var roles = new HashSet<String>();
        if (method.isAnnotationPresent(Roles.class)) {
            roles.addAll(Arrays.asList(method.getAnnotation(Roles.class).value()));
        }
        roles.addAll(getRequiredRoles(method.getDeclaringClass()));
        return roles;

    }

    public static Set<String> getRequiredRoles(Class<?> c) {
        var roles = new HashSet<Roles>();
        while (c != null && c != Object.class) {
            if (c.isAnnotationPresent(Roles.class)) {
                roles.add(c.getAnnotation(Roles.class));
            }
            c = c.getSuperclass();
        }
        return roles.stream()
                .flatMap(role -> Stream.of(role.value()))
                .collect(Collectors.toSet());
    }
}
