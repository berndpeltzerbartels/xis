package one.xis.server;

import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
class WidgetAttributes {

    private String id;

    /**
     * Required for micronfrontend-architecture. Allows page and widgets
     * to be hosted on different servers.
     */
    private String host;
}
