package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
class FrontletAttributes extends ComponentAttributes {

    private String id;

    /**
     * Required for distributed frontends. Allows pages and frontlets
     * to be hosted on different servers.
     */
    private String host;
}
