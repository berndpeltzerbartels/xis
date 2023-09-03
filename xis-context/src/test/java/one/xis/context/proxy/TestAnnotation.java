package one.xis.context.proxy;

import one.xis.context.XISProxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@XISProxy(handlerClass = TestInvocationHandler.class)
public @interface TestAnnotation {
}
