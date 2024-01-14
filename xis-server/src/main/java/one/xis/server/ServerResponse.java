package one.xis.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
    private int httpStatus;
    private String data;
    private String nextPageURL;
    private String nextWidgetId;
    private Map<String, Object> widgetParameters;
    private ValidatorMessages validatorMessages;
}
