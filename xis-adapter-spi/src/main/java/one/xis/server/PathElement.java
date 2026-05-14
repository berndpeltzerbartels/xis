package one.xis.server;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;


abstract class PathElement {
    abstract String normalized();

    @Getter
    @Setter
    private PathElement next;

    abstract String evaluate(Map<String, Object> pathVariables);
}
