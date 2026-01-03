package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.*;

@Qualifier // for micronaut
@Singleton // for micronaut
@org.springframework.stereotype.Component // for spring
@Component
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JavascriptExtension {
    String value();
}
