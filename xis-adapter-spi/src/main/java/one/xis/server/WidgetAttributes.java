package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
class WidgetAttributes extends ComponentAttributes {

    private String id;

    /**
     * Required for micro-frontend-architecture. Allows page and widgets
     * to be hosted on different servers.
     */
    private String host;
}
