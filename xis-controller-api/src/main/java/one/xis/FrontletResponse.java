package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class FrontletResponse implements Response {
    private Class<?> controllerClass;
    private String frontlet;
    private String targetContainer;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> frontletParameters = new HashMap<>();
    private final Collection<String> frontletsToReload = new HashSet<>();

    public FrontletResponse(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public FrontletResponse(@NonNull String frontlet) {
        this.frontlet = frontlet;
    }

    public FrontletResponse(@NonNull Class<?> controllerClass, @NonNull String targetContainer) {
        this.controllerClass = controllerClass;
        this.targetContainer = targetContainer;
    }

    public FrontletResponse(@NonNull String frontlet, @NonNull String targetContainer) {
        this.frontlet = frontlet;
        this.targetContainer = targetContainer;
    }

    public FrontletResponse() {
    }

    public FrontletResponse controllerClass(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public FrontletResponse frontlet(@NonNull String frontlet) {
        this.frontlet = frontlet;
        return this;
    }

    public FrontletResponse frontletParameter(@NonNull String name, @NonNull Object value) {
        frontletParameters.put(name, asString(value));
        return this;
    }

    public FrontletResponse targetContainer(String targetContainer) {
        this.targetContainer = targetContainer;
        return this;
    }

    public FrontletResponse reloadFrontlet(Class<?> frontletController) {
        if (!frontletController.isAnnotationPresent(Frontlet.class)) {
            throw new IllegalArgumentException("not a frontlet: " + frontletController);
        }
        var frontletAnnotation = frontletController.getAnnotation(Frontlet.class);
        String frontletId = frontletAnnotation.value().equals("") ? frontletController.getSimpleName() : frontletAnnotation.value();
        return reloadFrontlet(frontletId);
    }

    public FrontletResponse reloadFrontlet(String frontletId) {
        frontletsToReload.add(frontletId);
        return this;
    }

    public static FrontletResponse of(@NonNull Class<?> controllerClass, @NonNull String paramName, @NonNull Object paramValue) {
        return new FrontletResponse(controllerClass).frontletParameter(paramName, asString(paramValue));
    }

    public static FrontletResponse of(@NonNull String frontlet, @NonNull String paramName, @NonNull Object paramValue) {
        return new FrontletResponse(frontlet).frontletParameter(paramName, asString(paramValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
