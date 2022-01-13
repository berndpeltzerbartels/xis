package one.xis;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Validate(NotEmptyValidator.class)
@Target(ElementType.FIELD)
public @interface NotEmpty {
    String message() default "";

    String longMessage() default "";
}
