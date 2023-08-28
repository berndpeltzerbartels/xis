package one.xis.server;

import lombok.NonNull;
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
        return url.endsWith(".html") ? url : url + ".html";
    }


    public static Collection<Object> getAllPageControllers() {
        return AppContext.getInstance("one.xis").getSingletons().stream()
                .filter(o -> o.getClass().isAnnotationPresent(Page.class))
                .collect(Collectors.toSet());
    }
}
