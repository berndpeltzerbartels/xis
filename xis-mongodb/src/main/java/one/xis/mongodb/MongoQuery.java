package one.xis.mongodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Defines an explicit MongoDB JSON query for a repository method.
 *
 * <p>Arguments are inserted with positional placeholders such as {@code ?0}
 * and {@code ?1}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MongoQuery {
    /**
     * MongoDB query document as JSON.
     */
    String value();
}
