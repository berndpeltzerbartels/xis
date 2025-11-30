package one.xis.context;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@Builder
public class JavascriptResponse {
    public final String responseText;
    public final int status;
    public final Map<String, List<String>> headers;

    public void addResponseHeader(String name, String value) {
        headers.computeIfAbsent(name.toUpperCase(), k -> new ArrayList<>()).add(value);
    }

    public String getResponseHeader(String name) {
        List<String> values = getResponseHeaders(name);
        return values.isEmpty() ? null : values.get(0);
    }

    public String getAllResponseHeaders() {
        return headers.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> entry.getKey() + ": " + value))
                .collect(Collectors.joining("\r\n"));
    }


    private List<String> getResponseHeaders(String name) {
        return headers.getOrDefault(name.toUpperCase(), List.of());
    }

}