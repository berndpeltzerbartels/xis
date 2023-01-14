package one.xis.test.js;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class JSConstructor implements JSContext {
    private final List<String> args;

    JSConstructor(String... args) {
        this.args = Arrays.asList(args);
    }

}
