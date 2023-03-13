package one.xis.server;

import lombok.Data;

import java.util.Map;

@Data
public class Request {
    private Map<String, DataItem> data;
    private String clientId;
    private String userId;
    private String action;
    private String controllerId;
}
