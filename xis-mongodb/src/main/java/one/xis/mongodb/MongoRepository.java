package one.xis.mongodb;

import one.xis.context.Proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a MongoDB repository interface for XIS context proxy creation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Proxy(factory = MongoRepositoryProxyFactory.class)
public @interface MongoRepository {
}
