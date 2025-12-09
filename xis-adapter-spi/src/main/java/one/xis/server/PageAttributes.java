package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
class PageAttributes extends ComponentAttributes {

    private Path path;

    /**
     * Unique identifier of a page, represented by the
     * path having all path-variables replaced by an asterisk.
     * <p>
     * e.g. /product/*.html
     */
    private String normalizedPath;
    private boolean welcomePage;
    
    /**
     * Concrete URL for welcome page with path variables.
     * Only set if this is a welcome page AND has path variables.
     * e.g. /category/electronics.html
     */
    private String welcomePageUrl;
    
    private String host;
    private String pageJavascriptSource;

}
