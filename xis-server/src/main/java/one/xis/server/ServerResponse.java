package one.xis.server;


import lombok.Data;

@Data
public class ServerResponse {
    private final int httpStatus;
    private final String data;
    private final String nextPageURL;
    private final String nextWidgetId;
}
