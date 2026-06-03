package one.xis;

import jakarta.inject.Qualifier;
import jakarta.inject.Singleton;
import one.xis.context.Component;

import java.lang.annotation.*;

/**
 * Registers an HTML fragment that can be reused from page or frontlet templates.
 *
 * <p>The annotation value is the include key. If it is {@code "navigation"}, the include can be used
 * with element syntax:</p>
 * <pre><code>
 * &lt;xis:include name="navigation"/&gt;
 * </code></pre>
 * <p>or with attribute syntax:</p>
 * <pre><code>
 * &lt;div xis:include="navigation"&gt;&lt;/div&gt;
 * </code></pre>
 *
 * <p>The include key is not resolved as a free resource path from the template.
 * Only fragments explicitly registered with {@code @Include} can be inserted.
 * This keeps include access deliberate: application code decides which
 * fragments are safe to reuse.</p>
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
@ImportInstances
@Documented
public @interface Include {
    String value();
}
