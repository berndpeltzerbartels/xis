package one.xis;

import java.util.Collection;

/**
 * Interface to register HTML includes for use in pages and widgets.
 * <p>
 * Implement this interface and annotate with @XISComponent to make includes
 * available throughout the application.
 * <p>
 * Example:
 * <pre>
 * @XISComponent
 * public class MyIncludes implements IncludeRegistry {
 *     public Collection&lt;Include&gt; includes() {
 *         return List.of(
 *             new Include("navigation", "my/app/navigation.include.html"),
 *             new Include("header", "my/app/header.include.html")
 *         );
 *     }
 * }
 * </pre>
 */
public interface IncludeRegistry {
    
    /**
     * Returns all includes registered by this registry.
     * 
     * @return Collection of includes, never null but may be empty
     */
    Collection<Include> includes();
}
