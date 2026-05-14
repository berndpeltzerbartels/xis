package one.xis.processor;

import javax.lang.model.element.TypeElement;
import java.util.List;

class ControllerMethodReflection {
    private final TypeElement element;

    ControllerMethodReflection(TypeElement element) {
        this.element = element;
    }

    /**
     * Model data return values. Methods annotated with @ModelData. Key is the value of the annotation or the method name if
     * the annotation has no value. Iterable is true if the return type is an array or a collection.
     *
     * @return
     */
    List<ReturnValue> getModelDataReturnValues() {
        return List.of();
    }

    List<ReturnValue> getFormDataReturnValues() {
        return List.of();
    }


}
