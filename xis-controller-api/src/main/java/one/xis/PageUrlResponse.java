package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PageUrlResponse implements Response {
    private final String address;
    private final Map<String, Object> parameters;

    public PageUrlResponse(String address) {
        this(address, Map.of());
    }

    public String getUrl() {
        return address + (parameters.isEmpty() ? "" : "?" + parameters.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "&" + b).orElse(""));
    }

    @Override
    public Class<?> getControllerClass() {
        return null;
    }
}
