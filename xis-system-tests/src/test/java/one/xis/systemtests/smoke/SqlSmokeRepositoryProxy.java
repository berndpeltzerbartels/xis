package one.xis.systemtests.smoke;

import one.xis.context.Proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Proxy(factoryName = "one.xis.sql.SQLRepositoryProxyFactory")
@interface SqlSmokeRepositoryProxy {
}
