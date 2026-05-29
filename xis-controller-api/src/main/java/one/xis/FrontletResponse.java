package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Action result that loads or parameterizes frontlets.
 *
 * <p>Return this type from an {@link Action} method when the action should open a
 * frontlet in a container or pass {@link FrontletParameter frontlet parameters}.
 * A response can identify the target frontlet either by controller class or by
 * frontlet id/path. Use {@link #targetContainer(String)} when the target
 * container cannot be inferred from the current frontlet context or the frontlet
 * annotation.</p>
 *
 * <pre>{@code
 * @Action
 * FrontletResponse edit(@ActionParameter("id") long id) {
 *     return new FrontletResponse(CustomerFormFrontlet.class)
 *             .targetContainer("main")
 *             .frontletParameter("customerId", id);
 * }
 * }</pre>
 */
@Getter
@EqualsAndHashCode
public class FrontletResponse implements Response {
    private Class<?> controllerClass;
    private String frontlet;
    private String targetContainer;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> frontletParameters = new HashMap<>();

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

    /**
     * Sets the frontlet controller class to load.
     */
    public FrontletResponse controllerClass(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    /**
     * Sets the frontlet id or frontlet path to load.
     */
    public FrontletResponse frontlet(@NonNull String frontlet) {
        this.frontlet = frontlet;
        return this;
    }

    /**
     * Adds a parameter for the target frontlet instance. The receiving frontlet
     * reads it with {@link FrontletParameter}.
     */
    public FrontletResponse frontletParameter(@NonNull String name, @NonNull Object value) {
        frontletParameters.put(name, asString(value));
        return this;
    }

    /**
     * Sets the id of the frontlet container that should receive this response.
     */
    public FrontletResponse targetContainer(String targetContainer) {
        this.targetContainer = targetContainer;
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
