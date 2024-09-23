package one.xis.server;

import lombok.Data;

@Data
class PageAttributes {

    private Path path;

    /**
     * Unique identifier of a page, represented by the
     * path having all path-variables replaced by an asterisk.
     * <p>
     * e.g. /product/*.html
     */
    private String normalizedPath;
    private boolean welcomePage;
    private String host;

}
