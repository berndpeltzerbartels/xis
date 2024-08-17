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
            System.out.println(CollectionUtils.first(componentWrapper.getFieldWrappers()).getField());
        }
    }

    private void postCheckExecutables(Collection<? extends ExecutableWrapper<?>> executableWrappers) {
        if (!executableWrappers.isEmpty()) { // wrappers get removed when intantionation is completed and all methods are called
            var executable = CollectionUtils.first(executableWrappers); // if there is at leas one of them, context creation failed
            if (!executable.getParameters().isEmpty()) { // if there is at least one parameter, this is the reason why the method was not called
                throw new UnsatisfiedDependencyException(CollectionUtils.first(executable.getParameters()).getParameter());
            }
            throw new UnsatisfiedDependencyException(CollectionUtils.first(executable.getParameters()).getParameter());
        }
    }


}
