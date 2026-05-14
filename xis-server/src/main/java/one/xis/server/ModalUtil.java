package one.xis.server;

import lombok.NonNull;
import one.xis.Modal;

public class ModalUtil {

    public static String getId(@NonNull Object modalController) {
        return getId(modalController.getClass());
    }

    public static String getId(@NonNull Class<?> controllerClass) {
        var anno = controllerClass.getAnnotation(Modal.class);
        if (anno == null) {
            throw new IllegalArgumentException("not a modal-controller: " + controllerClass);
        }
        if (!anno.id().isEmpty()) {
            return anno.id();
        }
        return controllerClass.getSimpleName();
    }

    public static String getUrl(@NonNull Class<?> controllerClass) {
        var anno = controllerClass.getAnnotation(Modal.class);
        return anno != null ? anno.value() : "";
    }

    public static String getTitle(@NonNull Class<?> controllerClass) {
        var anno = controllerClass.getAnnotation(Modal.class);
        return anno != null ? anno.title() : "";
    }
}
