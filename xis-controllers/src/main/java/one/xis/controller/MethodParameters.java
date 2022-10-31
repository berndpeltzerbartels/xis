package one.xis.controller;

import java.util.ArrayList;
import java.util.List;

class MethodParameters {
    private final List<MethodParameter> parameters = new ArrayList<>();

    void addParameter(MethodParameter parameter) {
        parameters.add(parameter);
    }

}
