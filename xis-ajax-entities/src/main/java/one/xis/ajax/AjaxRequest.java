package one.xis.ajax;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
public class AjaxRequest {
    private Date timestamp;
    private ComponentType componentType;
    private List<AjaxRequestMessage> messages;
    private Map<String, String> urlParameters;
}
