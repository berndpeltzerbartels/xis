package one.xis;

import java.lang.annotation.*;

@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormData {

    String value();
}
