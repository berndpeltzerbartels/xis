package one.xis.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class Controller {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private Class<?> controllerClass;

    @JsonProperty("model-methods")
    private Map<String, ModelMethod> modelMethods;

    @JsonProperty("model-timestamp-methods")
    private Map<String, ModelTimestampMethod> modelTimestampMethods;

    @JsonProperty("action-methods")
    private Map<String, ActionMethod> actionMethods;
}
