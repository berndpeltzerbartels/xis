package one.xis.js;

import lombok.Value;

import java.util.Arrays;
import java.util.List;

@Value
public class JSContructorCall implements JSValue {
    JSClass jsClass;
    List<String> contructorParams;

    JSContructorCall(JSClass jsClass, String... contructorParams) {
        this.jsClass = jsClass;
        this.contructorParams = Arrays.asList(contructorParams);
    }
}
