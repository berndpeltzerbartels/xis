package one.xis.server;

import lombok.NonNull;
import one.xis.Route;
import one.xis.Router;

import java.lang.reflect.Method;

class RouterUtil {

    static String getBaseUrl(@NonNull Object controller) {
        return getBaseUrl(controller.getClass());
    }

    static String getBaseUrl(@NonNull Class<?> controllerClass) {
        var url = controllerClass.getAnnotation(Router.class).value();
        if (url.startsWith("/xis/")) {
            throw new IllegalStateException("Router url must not start with /xis/: " + url);
        }
        return normalizeStart(url);
    }

    static String getRouteUrl(@NonNull Object controller, @NonNull Method method) {
        return getRouteUrl(controller.getClass(), method);
    }

    static String getRouteUrl(@NonNull Class<?> controllerClass, @NonNull Method method) {
        var route = method.getAnnotation(Route.class).value();
        var url = combine(getBaseUrl(controllerClass), route);
        return url.endsWith(".html") ? url : url + ".html";
    }

    private static String combine(String base, String route) {
        if (route == null || route.isBlank()) {
            return base;
        }
        if (base.endsWith("/") && route.startsWith("/")) {
            return base + route.substring(1);
        }
        if (!base.endsWith("/") && !route.startsWith("/")) {
            return base + "/" + route;
        }
        return base + route;
    }

    private static String normalizeStart(String url) {
        return url.startsWith("/") ? url : "/" + url;
    }
}
