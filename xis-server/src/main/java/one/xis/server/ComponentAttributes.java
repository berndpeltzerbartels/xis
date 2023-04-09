package one.xis.server;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.Map;

@Data
@NoArgsConstructor
class ComponentAttributes {
    private Collection<String> modelsToSubmitForModel;
    private Map<String, Collection<String>> modelsToSubmitForAction;
}
