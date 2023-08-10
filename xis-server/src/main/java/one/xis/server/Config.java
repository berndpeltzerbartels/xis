package one.xis.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.Collection;
import java.util.Map;

/**
 * Every field is annotated to avoid micronaut ignores
 * empty arrays etc.
 */
@Data
@Builder
public class Config {

    @JsonInclude
    private final Collection<String> widgetIds;

    @JsonInclude
    private final Collection<String> pageIds;

    /**
     * Normalized path of the welcome-page.
     */
    @JsonInclude
    private final String welcomePageId;

    /**
     * Key is the normalized path
     *
     * @{@link Path}
     */
    @JsonInclude
    private final Map<String, PageAttributes> pageAttributes;

    /**
     * Key is the normalized path
     *
     * @{@link Path}
     */
    @JsonInclude
    private final Map<String, ComponentAttributes> widgetAttributes;


}
