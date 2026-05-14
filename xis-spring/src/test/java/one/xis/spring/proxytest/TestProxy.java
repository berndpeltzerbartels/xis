package one.xis.spring.proxytest;

import one.xis.context.Proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Proxy(factory = TestProxyFactory.class)
public @interface TestProxy {
}
