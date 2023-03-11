package one.xis.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Collection;

@Data
class InvocationContext {
    private JsonNode data;
    private String clientId;
    private String userId;
    private Collection<Integer> methodIds;
    private String controllerId;
    
    @JsonProperty("type")
    private InvocationType invocationType;
}
