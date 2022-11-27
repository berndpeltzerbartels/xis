package one.xis.ajax;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class AjaxRequestMessage {
    private String componentClass;
    private Phase phase;
    private AjaxRequestType type;
    private Map<String, String> componentParameters;
    private ClientAttributes clientAttributes;

    @JsonDeserialize(using = RawJsonDeserializer.class)
    String data;
}
