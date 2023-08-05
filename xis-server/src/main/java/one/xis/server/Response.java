package one.xis.server;


import lombok.Data;

import java.util.Map;

@Data
public class Response {
    private final int httpStatus;
    private final Map<String, Object> data;
    private final String nextPageURL;
    private final String nextWidgetId;
}
