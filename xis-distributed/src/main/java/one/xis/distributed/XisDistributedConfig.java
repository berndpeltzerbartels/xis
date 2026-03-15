package one.xis.distributed;

import one.xis.ImportInstances;

/**
 * Configuration interface for distributed / micro-frontend XIS deployments.
 * <p>
 * Annotated with {@link ImportInstances} so that Spring {@code @Bean} implementations
 * are automatically imported into the XIS context via {@code SpringContextAdapter}.
 *
 * <h3>Usage options</h3>
 * <ol>
 *   <li><b>application.properties (XIS-Boot)</b> – the built-in
 *       {@link PropertiesXisDistributedConfig} ({@code @DefaultComponent}) is picked up
 *       automatically when {@code xis-distributed} is on the classpath.</li>
 *   <li><b>Spring {@code @Bean}</b> – declare a bean implementing this interface;
 *       it replaces the default implementation and is imported into the XIS context.</li>
 *   <li><b>XIS-Boot {@code @Component}</b> – annotate your class with {@code @Component};
 *       it replaces the {@code @DefaultComponent} implementation.</li>
 * </ol>
 *
 * <h3>Properties format (option 1)</h3>
 * <pre>
 * # Mandatory – own server host, used as fallback for all local components
 * xis.host=https://app.example.com
 *
 * # Optional – explicit remote host per widget-id (class SimpleName by default)
 * xis.remote.widget.ProductWidget=https://shop.example.com
 *
 * # Optional – explicit remote host per normalised page path (* replaces path-variables)
 * xis.remote.page./product/*.html=https://shop.example.com
 * </pre>
 */
@ImportInstances
public interface XisDistributedConfig {

    /**
     * Returns the host (scheme + authority) for the given widget-id.
     * Falls back to {@code xis.host} if no explicit mapping exists.
     */
    String getWidgetHost(String widgetId);

    /**
     * Returns the host for the given normalised page path.
     * Falls back to {@code xis.host} if no explicit mapping exists.
     */
    String getPageHost(String normalizedPath);
}
