package one.xis.server;

import lombok.NonNull;
import one.xis.Frontlet;
import one.xis.Modal;
import one.xis.context.AppContext;

import java.util.Collection;
import java.util.stream.Collectors;

public class FrontletUtil {
    public static String getId(@NonNull Object frontletController) {
        return getId(frontletController.getClass());
    }

    public static String getId(@NonNull Class<?> controllerClass) {
        var modal = controllerClass.getAnnotation(Modal.class);
        if (modal != null) {
            return ModalUtil.getId(controllerClass);
        }
        @NonNull var anno = controllerClass.getAnnotation(Frontlet.class);
        if (!anno.id().isEmpty()) {
            return anno.id();
        }
        if (anno.value().isEmpty()) {
            return controllerClass.getSimpleName();
        }
        return anno.value();
    }

    public static String getUrl(@NonNull Class<?> controllerClass) {
        var modal = controllerClass.getAnnotation(Modal.class);
        if (modal != null) {
            return ModalUtil.getUrl(controllerClass);
        }
        var anno = controllerClass.getAnnotation(Frontlet.class);
        return anno != null ? anno.url() : "";
    }

    public static String getTitle(@NonNull Class<?> controllerClass) {
        var modal = controllerClass.getAnnotation(Modal.class);
        if (modal != null) {
            return ModalUtil.getTitle(controllerClass);
        }
        var anno = controllerClass.getAnnotation(Frontlet.class);
        return anno != null ? anno.title() : "";
    }

    public static String getContainerId(@NonNull Class<?> controllerClass) {
        var anno = controllerClass.getAnnotation(Frontlet.class);
        return anno != null ? anno.containerId() : "";
    }

    public static Collection<Object> getAllFrontletControllers() {
        return AppContext.getInstance("one.xis").getSingletons().stream()
                .filter(o -> o.getClass().isAnnotationPresent(Frontlet.class) || o.getClass().isAnnotationPresent(Modal.class))
                .collect(Collectors.toSet());
    }
}
