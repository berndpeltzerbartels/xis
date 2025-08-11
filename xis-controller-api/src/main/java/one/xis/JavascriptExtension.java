package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.XISComponent;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Qualifier // for micronaut
@Singleton // for micronaut
@Component // for spring
@XISComponent
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface JavascriptExtension {
    String value();
}
