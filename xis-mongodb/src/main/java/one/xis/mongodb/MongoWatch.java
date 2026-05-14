package one.xis.mongodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Starts a MongoDB change-stream watcher for a XIS component method.
 *
 * <p>The method must accept either {@link MongoChangeEvent} or the mapped
 * document type directly.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MongoWatch {
    /**
     * Mapped document type to watch.
     */
    Class<?> value();

    /**
     * Optional explicit collection name. If omitted, the collection from
     * {@link MongoDocument} is used.
     */
    String collection() default "";
}
