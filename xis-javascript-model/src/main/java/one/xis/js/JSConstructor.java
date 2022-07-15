package one.xis.js;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

@Data
public class JSConstructor implements JSContext {
    private final List<String> args;

    JSConstructor(String... args) {
        this.args = Arrays.asList(args);
    }

}
