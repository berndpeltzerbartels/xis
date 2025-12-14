package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a factory method that creates and registers a component.
 * This is the XIS equivalent of Spring's {@code @Bean} annotation.
 * 
 * <p>Methods annotated with {@code @Bean} must be declared in a component class (annotated with
 * {@code @Component} or similar). The method's return value is registered as a singleton in the
 * application context and can be injected into other components.</p>
 * 
 * <p>Bean methods can declare dependencies as method parameters, which will be injected by the context:</p>
 * 
 * <pre>
 * {@code @Component}
 * public class AppConfiguration {
 *     
 *     {@code @Bean}
 *     public DataSource createDataSource(ConfigService config) {
 *         return new DataSource(config.getDatabaseUrl());
 *     }
 *     
 *     {@code @Bean}
 *     public UserRepository createUserRepository(DataSource dataSource) {
 *         return new UserRepository(dataSource);
 *     }
 * }
 * </pre>
 * 
 * @see XISInit
 * @see XISComponent
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface XISBean {
}
