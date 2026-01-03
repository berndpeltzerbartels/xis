package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method to be called after the component is constructed and all dependencies are injected.
 * This is the XIS equivalent of Spring's {@code @PostConstruct} or Java EE's {@code @PostConstruct}.
 *
 * <p>The annotated method must be no-arg and can have any return type (which will be ignored).
 * If the method throws an exception, component initialization fails.</p>
 *
 * <p>Example:</p>
 * <pre>
 * {@code @Component}
 * public class DatabaseService {
 *     private final ConfigService config;
 *     private Connection connection;
 *
 *     public DatabaseService(ConfigService config) {
 *         this.config = config;
 *     }
 *
 *     {@code @Init}
 *     public void initialize() {
 *         this.connection = createConnection(config.getDatabaseUrl());
 *     }
 * }
 * </pre>
 *
 * @see Bean
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XISInit {
}
