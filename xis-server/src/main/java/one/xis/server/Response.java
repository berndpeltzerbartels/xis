package one.xis.server;


import lombok.Data;

import java.util.Map;

@Data
public class Response {
    private final Map<String, Object> data;
    private final String nextPageId;
    private final String nextWidgetId;
}
