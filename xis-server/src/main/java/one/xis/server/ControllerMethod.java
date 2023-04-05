package one.xis.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.UserId;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

@Data
@SuperBuilder
abstract class ControllerMethod {

    protected Method method;
    protected String key;
    protected ObjectMapper objectMapper;

    @SneakyThrows
    Object invoke(Request request, Object controller) {
        return method.invoke(controller, prepareArgs(request));
    }

    @Override
    public String toString() {
        return method.toString();
    }


    protected Object[] prepareArgs(Request context) {
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

    @SneakyThrows
    private Object modelParameter(Parameter parameter, Request context) {
        var key = parameter.getAnnotation(Model.class).value();
        var o = context.getData().get(key);
        if (o instanceof String) {
            if (parameter.getType() == String.class) {
                return o;
            }
            return objectMapper.readValue((String) o, parameter.getType());
        }
        return o;
    }
}
