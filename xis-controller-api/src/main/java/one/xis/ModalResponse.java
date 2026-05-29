package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Action result for opening or closing modal frontends.
 *
 * <p>Return this type from an {@link Action} method when the action should open a
 * modal, pass values to it, close the current modal, or reload the page/frontlet
 * that opened it. Parameters supplied through {@link #parameter(String, Object)}
 * are read in the modal controller with {@link ModalParameter}.</p>
 *
 * <pre>{@code
 * @Action
 * ModalResponse edit(@ActionParameter("customerId") long id) {
 *     return ModalResponse.open(CustomerModal.class)
 *             .parameter("customerId", id);
 * }
 * }</pre>
 */
@Getter
@EqualsAndHashCode
public class ModalResponse implements Response {

    private Class<?> controllerClass;
    private String modal;
    private boolean close;
    private boolean reloadParent;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> parameters = new HashMap<>();

    public ModalResponse() {
    }

    public ModalResponse(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public ModalResponse(@NonNull String modal) {
        this.modal = modal;
    }

    /**
     * Opens the given modal controller class.
     */
    public static ModalResponse open(@NonNull Class<?> controllerClass) {
        return new ModalResponse(controllerClass);
    }

    /**
     * Opens a modal by id or by a concrete modal path.
     */
    public static ModalResponse open(@NonNull String modal) {
        return new ModalResponse(modal);
    }

    /**
     * Closes the current modal.
     */
    public static ModalResponse close() {
        return new ModalResponse().closeModal();
    }

    public ModalResponse controllerClass(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public ModalResponse modal(@NonNull String modal) {
        this.modal = modal;
        return this;
    }

    /**
     * Adds a path variable for URL-based modal targets.
     */
    public ModalResponse pathVariable(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    /**
     * Adds a modal parameter. The modal controller reads it with
     * {@link ModalParameter}.
     */
    public ModalResponse parameter(@NonNull String name, @NonNull Object value) {
        parameters.put(name, asString(value));
        return this;
    }

    /**
     * Marks this response as closing the current modal.
     */
    public ModalResponse closeModal() {
        close = true;
        return this;
    }

    /**
     * Reloads the page or frontlet instance that opened the modal after the modal closes.
     */
    public ModalResponse reloadParent() {
        reloadParent = true;
        return this;
    }

    public static ModalResponse of(@NonNull Class<?> controllerClass, @NonNull String paramName, @NonNull Object paramValue) {
        return open(controllerClass).parameter(paramName, paramValue);
    }

    public static ModalResponse of(@NonNull String modal, @NonNull String paramName, @NonNull Object paramValue) {
        return open(modal).parameter(paramName, paramValue);
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
