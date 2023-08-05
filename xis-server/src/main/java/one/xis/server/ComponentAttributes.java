package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
class ComponentAttributes {
    private String host;
    private Collection<String> modelsToSubmitOnRefresh;
    private Map<String, Collection<String>> modelsToSubmitOnAction;
}
