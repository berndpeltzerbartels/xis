package one.xis.i18n;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(ElementType.TYPE)
public @interface Mapped { // TODO remove hole module
    Class<? extends Mapper<?>> value();
}
