package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.CollectionUtils;

import java.util.Collection;

@RequiredArgsConstructor
class ComponentWrapperPostCheck {
    private final ComponentWrapper componentWrapper;

    void postCheck() {
        postCheckDependencyFields();
        postCheckInitMethods();
        postCheckBeanMethods();
    }

    private void postCheckInitMethods() {
        postCheckExecutables(componentWrapper.getInitMethods());
    }

    private void postCheckBeanMethods() {
        postCheckExecutables(componentWrapper.getBeanMethods());
    }

    private void postCheckDependencyFields() {
        if (!componentWrapper.getFieldWrappers().isEmpty()) {
            throw new UnsatisfiedDependencyException(CollectionUtils.first(componentWrapper.getFieldWrappers()).getField());
        }
    }

    private void postCheckExecutables(Collection<? extends ExecutableWrapper<?>> executableWrappers) {
        if (!executableWrappers.isEmpty()) {
            var executable = CollectionUtils.first(executableWrappers);
            if (executable.getParameters().isEmpty()) {
                throw new IllegalStateException();
            }
            throw new UnsatisfiedDependencyException(CollectionUtils.first(executable.getParameters()).getParameter());
        }
    }


}
