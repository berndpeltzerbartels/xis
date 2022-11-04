package one.xis.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public abstract class Request {
    private String clientId;
    private String token;
    private String controllerClass;
    private String componentId;
    private Date timestamp;
    private Map<String, Object> clientState;
}
