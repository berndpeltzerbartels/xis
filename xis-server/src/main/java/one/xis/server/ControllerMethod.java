package one.xis.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.UserId;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@Data
@SuperBuilder
abstract class ControllerMethod {

    @JsonIgnore
    protected Object controller;

    @JsonIgnore
    protected Method method;

    @JsonProperty("parameters")
    protected List<MethodParameter> methodParameters;

    @SneakyThrows
    Object invoke(InvocationContext context) {
        return method.invoke(controller, prepareArgs(context));
    }

    @JsonProperty("type")
    abstract InvocationType getInvocationType();

    @Override
    public String toString() {
        return method.toString();
    }


    protected Object[] prepareArgs(InvocationContext context) {
        Object[] args = new Object[method.getParameterCount()];
        var params = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            var param = params[i];
            if (param.isAnnotationPresent(Model.class)) {
                args[i] = modelParameter(param, context);
            } else if (param.isAnnotationPresent(UserId.class)) {
                args[i] = context.getUserId();
            } else if (param.isAnnotationPresent(ClientId.class)) {
                args[i] = context.getClientId();
            } else {
                throw new IllegalStateException("no annotation: " + param);
            }
        }
        return args;
    }

    private Object modelParameter(Parameter parameter, InvocationContext context) {
        var key = parameter.getAnnotation(Model.class).value();
        return context.getData().get(key);
    }
}
