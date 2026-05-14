package one.xis.http;

import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class MethodMatchResult {

    private final boolean match;
    private final Map<String, String> pathVariables;

    private MethodMatchResult(boolean match, Map<String, String> pathVariables) {
        this.match = match;
        this.pathVariables = Collections.unmodifiableMap(pathVariables);
    }

    public static MethodMatchResult noMatch() {
        return new MethodMatchResult(false, Collections.emptyMap());
    }

    public static MethodMatchResult match(Map<String, String> pathVariables) {
        return new MethodMatchResult(true, pathVariables);
    }
}