package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * Action result that navigates the browser to a concrete page URL.
 *
 * <p>Use this response when the target is known as a URL string instead of a
 * {@link Page} controller class. Query parameters supplied in the constructor
 * are appended to the URL by {@link #getUrl()}.</p>
 */
@Getter
@RequiredArgsConstructor
public class PageUrlResponse implements Response {
    private final String address;
    private final Map<String, Object> parameters;

    public PageUrlResponse(String address) {
        this(address, Map.of());
    }

    /**
     * Returns the URL including query parameters, if any.
     */
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
