package one.xis.context;

import lombok.Data;
import one.xis.validation.ValidatorMessages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class BackendBridgeResponse {
    public final String responseText;
    public final int status;
    public final ValidatorMessages validatorMessages;
    public final Map<String, List<String>> headers = new HashMap<>();
    
    public void addResponseHeader(String name, String value) {
        headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
    }

    public List<String> getResponseHeader(String name) {
        return headers.getOrDefault(name, List.of());
    }

    public String getAllResponseHeaders() {
        return headers.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> entry.getKey() + ": " + value))
                .collect(Collectors.joining("\r\n"));
    }
}