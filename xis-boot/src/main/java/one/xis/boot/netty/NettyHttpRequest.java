package one.xis.boot.netty;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import one.xis.http.ContentType;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class NettyHttpRequest implements HttpRequest {

    private final FullHttpRequest request;
    private final QueryStringDecoder queryStringDecoder;
    private byte[] body;
    private Map<String, String> queryParameters;
    private Map<String, String> formParameters;

    public NettyHttpRequest(FullHttpRequest request) {
        this.request = request;
        this.queryStringDecoder = new QueryStringDecoder(request.uri());
    }

    @Override
    public String getPath() {
        return queryStringDecoder.path();
    }

    @Override
    public String getRealPath() {
        return queryStringDecoder.path(); // In Netty, there's no context path, so it's the same as getPath()
    }

    @Override
    public Map<String, String> getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = queryStringDecoder.parameters().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0), (a, b) -> b));
        }
        return queryParameters;
    }

    @Override
    public byte[] getBody() {
        if (body == null) {
            int length = request.content().readableBytes();
            body = new byte[length];
            request.content().getBytes(request.content().readerIndex(), body);
        }
        return body;
    }

    @Override
    public String getBodyAsString() {
        return new String(getBody(), StandardCharsets.UTF_8);
    }

    @Override
    public ContentType getContentType() {
        String contentTypeHeader = request.headers().get("Content-Type");
        if (contentTypeHeader == null) {
            return null;
        }
        for (ContentType ct : ContentType.values()) {
            if (contentTypeHeader.toLowerCase().startsWith(ct.getValue().toLowerCase())) {
                return ct;
            }
        }
        return null;
    }

    @Override
    public int getContentLength() {
        return request.content().readableBytes();
    }

    @Override
    public Collection<String> getHeaderNames() {
        return request.headers().names().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
    }

    @Override
    public String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.valueOf(request.method().name().toUpperCase());
    }

    @Override
    public Object getBodyAsBytes() {
        return getBody();
    }

    @Override
    public Map<String, String> getFormParameters() {
        if (formParameters == null) {
            if (ContentType.FORM_URLENCODED.equals(getContentType())) {
                QueryStringDecoder formDecoder = new QueryStringDecoder("?" + getBodyAsString());
                formParameters = formDecoder.parameters().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().get(0), (a, b) -> b));
            } else {
                formParameters = Map.of();
            }
        }
        return formParameters;
    }

    @Override
    public Locale getLocale() {
        String acceptLanguage = request.headers().get("Accept-Language");
        if (acceptLanguage == null) {
            return Locale.getDefault();
        }
        return Locale.forLanguageTag(acceptLanguage.split(",")[0]);
    }
}