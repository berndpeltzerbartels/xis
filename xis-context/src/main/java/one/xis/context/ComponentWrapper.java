package one.xis.context;

import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;


class ComponentWrapper {

    private final Queue<MethodWrapper> methodWrappers;
    private final Queue<FieldWrapper> fieldWrappers;

    private final Collection<MethodWrapper> executableMethods = new HashSet<>();

    ComponentWrapper(Queue<MethodWrapper> methodWrappers, Queue<FieldWrapper> fieldWrappers) {
        this.methodWrappers = methodWrappers;
        this.fieldWrappers = fieldWrappers;
        this.methodWrappers.forEach(methodWrapper -> methodWrapper.setComponentWrapper(this));
    }

    Collection<MethodWrapper> removeExecutableMethods() {
        var rv = new HashSet<>(executableMethods);
        executableMethods.clear();
        return rv;
    }

    void removeExecutableMethod(MethodWrapper methodWrapper) {
        executableMethods.remove(methodWrapper);
    }

    boolean isDone() {
        return methodWrappers.isEmpty() && isDependencyFieldsInjected();
    }

    boolean isDependencyFieldsInjected() {
        return fieldWrappers.isEmpty();
    }

    void onComponentCreated(Object o) {
        for (var methodWrapper : methodWrappers) {
            methodWrapper.onComponentCreated(o);
            if (methodWrapper.isExecutable()) {
                methodWrappers.remove(methodWrapper);
                executableMethods.add(methodWrapper);
            }
        }
        for (var fieldWrapper : fieldWrappers) {
            fieldWrapper.onComponentCreated(o);
            if (fieldWrapper.isInjected()) {
                fieldWrappers.remove(fieldWrapper);
            }
        }
    }
}
