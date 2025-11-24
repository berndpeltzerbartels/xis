package one.xis;


import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RefreshOnUpdateEvents {
    String[] value() default {};
}
