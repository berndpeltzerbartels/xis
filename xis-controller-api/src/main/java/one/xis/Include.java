package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.*;

/**
 * Registers an HTML fragment that can be reused from page or frontlet templates.
 *
 * <p>If the annotation value is {@code "navigation"}, the include can be used
 * with element syntax:</p>
 * <pre><code>
 * &lt;xis:include name="navigation"/&gt;
 * </code></pre>
 * <p>or with attribute syntax:</p>
 * <pre><code>
 * &lt;div xis:include="navigation"&gt;&lt;/div&gt;
 * </code></pre>
 *
 * <p>Includes are for markup reuse. The included markup is initialized as part
 * of the surrounding page or frontlet and can use that surrounding controller's
 * model data, actions, links, and parameters. Use {@link Frontlet} when the
 * fragment needs its own controller state.</p>
 *
 * <p>Classes annotated with {@code @Include} cannot be interfaces or abstract
 * classes.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
@Singleton
@org.springframework.stereotype.Component // for spring
@Component
@Documented
public @interface Include {
    String value();
}
