package one.xis.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
class PageAttributes extends ComponentAttributes {

    @JsonInclude
    private Path path;

    @JsonInclude
    private String normalizedPath;

    @JsonInclude
    private boolean welcomePage;

}
