package one.xis.server;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ServerResponse {
    private final int httpStatus;
    private final String data;
    private final String nextPageURL;
    private final String nextWidgetId;
    private final Map<String, Object> widgetParameters;

    @JsonProperty("validation")
    private final ValidationResult validationResult;
}
