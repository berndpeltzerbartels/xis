package one.xis;

import java.lang.annotation.*;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// TODO: may be remove it
public @interface CssFile {
    String value();
}
