package one.xis.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ComponentConfig {

    @JsonProperty("widgets")
    private final Map<String, Controller> widgetControllerMap;

    @JsonProperty("pages")
    private final Map<String, Controller> pageControllerMap;
    
}
