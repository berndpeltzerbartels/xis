package one.xis.server;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
public class Controller {

    @JsonIgnore
    private String id;

    @JsonIgnore
    private Class<?> controllerClass;

    @JsonProperty("model-methods")
    private Collection<ControllerMethod> modelMethods;

    @JsonProperty("action-methods")
    private Map<String, ControllerMethod> actionMethods;
}
