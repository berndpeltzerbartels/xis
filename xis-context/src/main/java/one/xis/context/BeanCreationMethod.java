package one.xis.context;

import lombok.extern.slf4j.Slf4j;
import one.xis.utils.lang.MethodUtils;

import java.lang.reflect.Method;
import java.util.Optional;

@Slf4j
class BeanCreationMethod extends SingletonMethod {
    BeanCreationMethod(Method method, SingletonWrapper parent, ParameterFactory parameterFactory) {
        super(method, parent, parameterFactory);
    }

    @Override
    public void invoke() {
        if (log.isDebugEnabled()) {
            log.debug("invoking bean method {} of {}", getMethod().getName(), getParent().getBeanClass().getName());
        }
        super.invoke();
    }

    @Override
    public boolean isInvocable() {
        if (getParent().getBean() == null) return false;
        if (!getParent().getSingletonFields().isEmpty()) return false;
        if (!getParent().getInitMethods().isEmpty()) return false;
        return super.isInvocable();
    }

    @Override
    public Class<?> getSingletonClass() {
        var returnType = getMethod().getReturnType();
        if (returnType.equals(Optional.class)) {
            return MethodUtils.getGenericTypeParameterOfReturnType(getMethod());
        }
        return returnType;
    }

    @Override
    public String toString() {
        return "BeanCreationMethod{" + getMethod().getDeclaringClass().getSimpleName() + "." + getMethod().getName() + "}";
    }
}
