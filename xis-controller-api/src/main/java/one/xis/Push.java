package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.XISProxy;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@XISProxy(factoryName = "one.xis.server.PushClientProxyFactory")
@Component
@Qualifier // for micronaut
@Singleton // for micronaut
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Push { // TODO validate path-variables are present
    Class<?> value();
}
