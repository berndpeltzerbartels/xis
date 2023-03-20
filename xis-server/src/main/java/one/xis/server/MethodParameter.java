package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.ClientId;
import one.xis.Model;
import one.xis.PathElement;
import one.xis.UserId;

import java.lang.reflect.Parameter;

@Data
@NoArgsConstructor
class MethodParameter {

    private ParameterType parameterType;

    private String key;

    static MethodParameter createParameter(Parameter parameter) {
        var methodParameter = new MethodParameter();
        if (parameter.isAnnotationPresent(Model.class)) {
            methodParameter.setParameterType(ParameterType.MODEL);
        } else if (parameter.isAnnotationPresent(ClientId.class)) {
            methodParameter.setParameterType(ParameterType.CLIENT_ID);
        } else if (parameter.isAnnotationPresent(UserId.class)) {
            methodParameter.setParameterType(ParameterType.USER_ID);
        } else if (parameter.isAnnotationPresent(PathElement.class)) {
            methodParameter.setParameterType(ParameterType.USER_ID);
        } else {
            throw new IllegalStateException("No known annotation for method-parameter: " + parameter);
        }
        return methodParameter;
    }
}
