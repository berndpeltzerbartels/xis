package one.xis.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import one.xis.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

@Data
@With
@AllArgsConstructor
@NoArgsConstructor
class ControllerMethod {

    @JsonIgnore
    private Object controller;

    @JsonIgnore
    private Method method;

    private int id;

    @JsonProperty("parameters")
    private List<MethodParameter> methodParameters;

    private String key;

    @JsonProperty("type")
    private InvocationType invocationType;

    @SneakyThrows
    Object invoke(InvocationContext context) {
        return method.invoke(controller, prepareArgs(context));
    }

    boolean isWidget() {
        return controller.getClass().isAnnotationPresent(Widget.class);
    }

    boolean isPage() {
        return controller.getClass().isAnnotationPresent(Page.class);
    }

    boolean isModel() {
        return method.isAnnotationPresent(Model.class);
    }

    boolean isAction() {
        return method.isAnnotationPresent(Action.class);
    }


    @Override
    public String toString() {
        return method.toString();
    }


    private Object[] prepareArgs(InvocationContext context) {
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
        var key = method.getAnnotation(Model.class).value();
        return context.getData().get(key);
    }
}
