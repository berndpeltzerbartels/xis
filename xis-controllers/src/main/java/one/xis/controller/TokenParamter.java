package one.xis.controller;

import lombok.Data;
import one.xis.dto.Request;

@Data
class TokenParamter implements MethodParameter<String> {
    private String value;

    @Override
    public String valueFromRequest(Request request) {
        value = request.getToken();
        return value;
    }
}
