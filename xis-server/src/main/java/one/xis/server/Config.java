package one.xis.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Config {

    @JsonProperty("widgets")
    private final List<String> widgetIds;

    @JsonProperty("pages")
    private final List<String> pageIds;

    private final Map<String, String> widgetHosts;

    private final Map<String, String> pageHosts;

    private final String welcomePageId;

}
