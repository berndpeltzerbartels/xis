package one.xis.context;

import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;


class InitMethodWrapper extends ExecutableWrapper<Method> {
    private final Method method;
    private final ComponentWrapperPlaceholder placeholder;

    InitMethodWrapper(Method method, ComponentWrapperPlaceholder placeholder) {
        super(method);
        this.method = method;
        this.placeholder = placeholder;
    }

    void execute(Object component) {
        MethodUtils.invoke(component, method, getArgs());
    }

    @Override
    boolean parameterAssigned(Object o, ParameterWrapper parameter) {
        super.parameterAssigned(o, parameter);
        if (isPrepared()) {
            placeholder.initMethodParamtersSet(this);
            return true;
        }
        return false;
    }
}
