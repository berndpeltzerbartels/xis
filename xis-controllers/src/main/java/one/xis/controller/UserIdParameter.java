package one.xis.controller;

import lombok.Data;
import one.xis.dto.Request;

@Data
class UserIdParameter implements MethodParameter<String> {
    private String value;

    @Override
    public String valueFromRequest(Request request) {
        value = extractuserId(request.getToken());
        return value;
    }

    private String extractuserId(String token) {
        return null; // TODO
    }
}
