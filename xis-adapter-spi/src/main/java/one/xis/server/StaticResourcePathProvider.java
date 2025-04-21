package one.xis.server;

import one.xis.ImportInstances;

/**
 * Interface to provide a custom static resource path. Each framework like Spring or Micronaut
 * has its own way of serving static resources.
 */
@ImportInstances
public interface StaticResourcePathProvider {
    /**
     * Returns the path to the static resources.
     *
     * @return the path to the static resources
     */
    String getCustomStaticResourcePath();
}
