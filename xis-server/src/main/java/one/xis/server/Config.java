package one.xis.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Config {

    @JsonProperty("widgets")
    private final Map<String, ControllerWrapper> widgetControllerMap;

    @JsonProperty("pages")
    private final Map<String, ControllerWrapper> pageControllerMap;

    private final String welcomePageId;

}
