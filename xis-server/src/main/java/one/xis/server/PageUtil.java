package one.xis.server;

import lombok.NonNull;
import one.xis.JavascriptFile;
import one.xis.Page;
import one.xis.context.AppContext;

import java.util.Collection;
import java.util.stream.Collectors;

public class PageUtil {

    public static String getUrl(@NonNull Object controller) {
        return getUrl(controller.getClass());
    }

    public static String getUrl(@NonNull Class<?> controllerClass) {
        var url = controllerClass.getAnnotation(Page.class).value();
        if (url.startsWith("/xis/")) {
            throw new IllegalStateException("Page url must not start with /xis/: " + url);
        }
        return url.endsWith(".html") ? url : url + ".html";
    }

    public static String getJavascriptResourcePath(Class<?> pageControllerClass) {
        var path = new StringBuilder(pageControllerClass.getPackageName().replace('.', '/')).append("/");
        if (pageControllerClass.isAnnotationPresent(JavascriptFile.class)) {
            path.append(pageControllerClass.getAnnotation(JavascriptFile.class).value());
        } else {
            path.append(pageControllerClass.getSimpleName());
        }
        if (!path.toString().endsWith(".js")) {
            path.append(".js");
        }
        return path.toString();
    }

    public static String getJavascriptResourcePath(Object pageController) {
        return getJavascriptResourcePath(pageController.getClass());
    }


    public static Collection<Object> getAllPageControllers() {
        return AppContext.getInstance("one.xis").getSingletons().stream()
                .filter(o -> o.getClass().isAnnotationPresent(Page.class))
                .collect(Collectors.toSet());
    }
}
