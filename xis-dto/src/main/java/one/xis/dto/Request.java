package one.xis.dto;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class Request {
    private String userId;
    private String clientId;
    private String token;
    private String controllerId;
    private String componentId;
    private Date timestamp;
    private Map<String, Object> componentModel;
    private Map<String, Object> clientState;
}
