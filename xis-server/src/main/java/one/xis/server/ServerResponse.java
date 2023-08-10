package one.xis.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
public class ServerResponse {
    private final int httpStatus;

    @JsonInclude
    private final Map<String, Object> data;
    private final String nextPageURL;
    private final String nextWidgetId;
}
