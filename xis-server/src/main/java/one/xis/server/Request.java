package one.xis.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class Request {

    @JsonDeserialize(using = DataDeserializer.class)
    private Map<String, String> data;
    private String clientId;
    private String userId;
    private String action;
    private String pageId;
    private String widgetId;
}
