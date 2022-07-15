package one.xis.js;

import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Value
public class JSContructorCall implements JSValue {
    JSClass jsClass;
    List<String> contructorParams;

    JSContructorCall(JSClass jsClass, String... contructorParams) {
        this.jsClass = jsClass;
        this.contructorParams = Arrays.asList(contructorParams);
        int argsInConstructor = this.jsClass.getConstructor().getArgs().size();
        int argsInConstructorCall = contructorParams.length;
        if (argsInConstructorCall != argsInConstructor) {
            String derrived = Optional.ofNullable(jsClass.getSuperClass()).map(JSClass::getClassName).map(name -> "extends " + name).orElse("");
            throw new IllegalStateException(String.format("number of args in contructor call (%d) does not match number of args in constructor of %s %s (%d)", argsInConstructorCall, jsClass.getClassName(), derrived, argsInConstructor));
        }
    }
}
