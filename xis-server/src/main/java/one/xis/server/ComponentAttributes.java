package one.xis.server;

import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@lombok.Data
@NoArgsConstructor
class ComponentAttributes {

    /**
     * Required for micronfrontend-architecture. Allows page and widgets
     * to be hosted on different servers.
     */
    private String host;

    /**
     * Keys of the data used as a parameter in methods annotated with @ModelData.
     * This filering is mainly to avoid transmission of transmissions
     * of data not required on server side.
     */
    private Collection<String> modelParameterNames;

    /**
     * Keys of the data used as a parameter in action-methods.
     * This filering is mainly to avoid transmission of transmissions
     * of data not required on server side.
     */
    private Map<String, Collection<String>> actionParameterNames;
}
