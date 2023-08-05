package one.xis.server;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class PageAttributes extends ComponentAttributes {
    private Path path;
    private String normalizedPath;
    private boolean welcomePage;

}
