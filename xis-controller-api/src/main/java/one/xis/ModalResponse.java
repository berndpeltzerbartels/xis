package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

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
     * Opens the given modal controller.
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

    public ModalResponse pathVariable(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

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
