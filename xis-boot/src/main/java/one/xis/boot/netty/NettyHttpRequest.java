package one.xis.boot.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import one.xis.http.ContentType;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class NettyHttpRequest implements HttpRequest {

    private final FullHttpRequest request;
    private final ChannelHandlerContext ctx;
    private final QueryStringDecoder queryStringDecoder;


    private String cachedBody;
    private Map<String, String> queryParameters;
    private Map<String, String> formParameters;

    public NettyHttpRequest(FullHttpRequest request, ChannelHandlerContext ctx) {
        this.request = Objects.requireNonNull(request, "request");
        this.ctx = Objects.requireNonNull(ctx, "ctx");
        queryStringDecoder = new QueryStringDecoder(request.uri(), StandardCharsets.UTF_8);
    }

    @Override
    public HttpMethod getHttpMethod() {
        return HttpMethod.valueOf(request.method().name());
    }

    @Override
    public String getPath() {
        String uri = request.uri();
        int q = uri.indexOf('?');
        return q >= 0 ? uri.substring(0, q) : uri;
    }

    @Override
    public String getBodyAsString() {
        if (cachedBody != null) {
            return cachedBody;
        }
        if (!request.content().isReadable()) {
            cachedBody = "";
            return cachedBody;
        }
        cachedBody = request.content().toString(StandardCharsets.UTF_8);
        return cachedBody;
    }

    @Override
    public ContentType getContentType() {
        return request.headers().get("Content-Type") != null ?
                ContentType.fromValue(request.headers().get("Content-Type")) :
                null;
    }

    @Override
    public int getContentLength() {
        return request.content().readableBytes();
    }

    @Override
    public String getHeader(String name) {
        return request.headers().get(name);
    }

    @Override
    public Map<String, String> getQueryParameters() {
        if (queryParameters == null) {
            queryParameters = parseQueryParameters(request.uri());
        }
        return queryParameters;
    }

    @Override
    public byte[] getBody() {
        return request.content().array();
    }

    @Override
    public Map<String, String> getFormParameters() {
        if (formParameters == null) {
            formParameters = parseFormParametersIfPresent();
        }
        return formParameters;
    }

    @Override
    public Locale getLocale() {
        String acceptLanguage = request.headers().get("Accept-Language");
        if (acceptLanguage == null || acceptLanguage.isBlank()) {
            return Locale.getDefault();
        }
        int comma = acceptLanguage.indexOf(',');
        String tag = (comma >= 0 ? acceptLanguage.substring(0, comma) : acceptLanguage).trim();
        return tag.isEmpty() ? Locale.getDefault() : Locale.forLanguageTag(tag);
    }

    /**
     * Returns the client IP (supports reverse proxies via X-Forwarded-For).
     */
    @Override
    public String getRemoteHost() {
        String xff = request.headers().get("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }

        if (ctx.channel().remoteAddress() instanceof InetSocketAddress addr && addr.getAddress() != null) {
            return addr.getAddress().getHostAddress();
        }
        return "unknown";
    }

    private Map<String, String> parseQueryParameters(String uri) {
        QueryStringDecoder decoder = new QueryStringDecoder(uri, StandardCharsets.UTF_8);
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            return Map.of();
        }

        Map<String, String> flat = new HashMap<>(params.size());
        for (var e : params.entrySet()) {
            List<String> values = e.getValue();
            flat.put(e.getKey(), (values == null || values.isEmpty()) ? null : values.get(0));
        }
        return Collections.unmodifiableMap(flat);
    }

    private Map<String, String> parseFormParametersIfPresent() {
        // Only parse form params when content-type is actually form-urlencoded.
        String ct = request.headers().get("Content-Type");
        if (ct == null) {
            return Map.of();
        }

        String lower = ct.toLowerCase(Locale.ROOT);
        boolean isFormUrlEncoded = lower.startsWith(ContentType.FORM_URLENCODED.toString());
        if (!isFormUrlEncoded) {
            return Map.of();
        }

        // Body is small/aggregated (your pipeline uses HttpObjectAggregator).
        // If you later switch to streaming, this logic must be changed.
        String body = getBodyAsString();
        if (body.isBlank()) {
            return Map.of();
        }

        // Parse "a=1&b=2" via QueryStringDecoder trick by prefixing "?".
        QueryStringDecoder decoder = new QueryStringDecoder("?" + body, StandardCharsets.UTF_8);
        Map<String, List<String>> params = decoder.parameters();
        if (params.isEmpty()) {
            return Map.of();
        }

        Map<String, String> flat = new HashMap<>(params.size());
        for (var e : params.entrySet()) {
            List<String> values = e.getValue();
            flat.put(e.getKey(), (values == null || values.isEmpty()) ? null : values.get(0));
        }
        return Collections.unmodifiableMap(flat);
    }
}
