package one.xis.context;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XISInject {
    Class<? extends Annotation> annotatedWith() default None.class;

    boolean optional() default false;
}
