package one.xis.context;

import one.xis.http.ContentType;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HttpTestRequest implements HttpRequest {

    private final HttpMethod method;
    private final String path;
    private final Map<String, String> queryParameters;
    private final byte[] body;
    private final Map<String, String> headers;
    private final ContentType contentType;

    public HttpTestRequest(HttpMethod method, String uri, String requestJson, Map<String, String> headers) {
        this.method = method;
        this.headers = headers != null ? new HashMap<>(headers) : new HashMap<>();
        this.body = requestJson != null ? requestJson.getBytes(StandardCharsets.UTF_8) : new byte[0];
        if (this.getHeader("Content-Type") == null) {
            this.headers.put("Content-Type", ContentType.JSON_UTF8.getValue());
        }
        String tempPath = uri;
        int queryIndex = uri.indexOf('?');
        if (queryIndex != -1) {
            tempPath = uri.substring(0, queryIndex);
            String query = uri.substring(queryIndex + 1);
            this.queryParameters = parseUrlEncoded(query);
        } else {
            this.queryParameters = Map.of();
        }
        this.path = tempPath;
        // ContentType.fromString wurde in der Enum-Datei zu fromValue geändert
        this.contentType = ContentType.fromValue(getHeader("Content-Type"));
    }

    private Map<String, String> parseUrlEncoded(String encodedString) {
        if (encodedString == null || encodedString.isEmpty()) {
            return new HashMap<>();
        }
        Map<String, String> parameters = new HashMap<>();
        String[] pairs = encodedString.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : null;
            parameters.put(key, value);
        }
        return parameters;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return Collections.unmodifiableMap(queryParameters);
    }

    @Override
    public byte[] getBody() {
        return body;
    }

    @Override
    public String getBodyAsString() {
        return HttpRequest.super.getBodyAsString();
    }

    @Override
    public ContentType getContentType() {
        return contentType;
    }

    @Override
    public int getContentLength() {
        return body.length;
    }

    @Override
    public String getHeader(String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    public HttpMethod getHttpMethod() {
        return method;
    }


    @Override
    public Map<String, String> getFormParameters() {
        if (contentType == ContentType.FORM_URLENCODED) {
            return Collections.unmodifiableMap(parseUrlEncoded(new String(body, StandardCharsets.UTF_8)));
        }
        return Map.of();
    }

    @Override
    public Locale getLocale() {
        String languageTag = getHeader("Accept-Language");
        return languageTag != null ? Locale.forLanguageTag(languageTag.split(",")[0]) : Locale.getDefault();
    }

    @Override
    public String getSuffix() {
        return HttpRequest.super.getSuffix();
    }

    @Override
    public String getRemoteHost() {
        return "localhost";
    }

    @Override
    public void addHeader(String name, String value) {
        headers.put(name, value);
    }
}