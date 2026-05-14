package one.xis.distributed;

import one.xis.context.Component;
import one.xis.http.FilterChain;
import one.xis.http.HttpFilter;
import one.xis.http.HttpMethod;
import one.xis.http.HttpRequest;
import one.xis.http.HttpResponse;
import one.xis.server.LocalUrlHolder;

import java.net.URI;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CORS policy for distributed XIS deployments.
 * <p>
 * The default HTTP controller filter mirrors the request origin. In a distributed
 * XIS application the allowed origins are part of the deployment topology, so
 * they are resolved from {@link XisDistributedConfig}.
 */
@Component
public class DistributedCorsFilter implements HttpFilter {

    private final LocalUrlHolder localUrlHolder;
    private final Set<String> configuredAllowedOrigins;

    public DistributedCorsFilter(XisDistributedConfig config, LocalUrlHolder localUrlHolder) {
        this.localUrlHolder = localUrlHolder;
        this.configuredAllowedOrigins = Set.copyOf(config.getAllowedOrigins());
    }

    @Override
    public void doFilter(HttpRequest request, HttpResponse response, FilterChain chain) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.isBlank()) {
            chain.doFilter(request, response);
            return;
        }

        String normalizedOrigin;
        try {
            normalizedOrigin = normalizeOrigin(origin);
        } catch (IllegalArgumentException ignored) {
            response.setStatusCode(403);
            return;
        }

        if (!allowedOrigins().contains(normalizedOrigin)) {
            response.setStatusCode(403);
            return;
        }

        response.addHeader("Access-Control-Allow-Origin", origin);
        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (HttpMethod.OPTIONS == request.getHttpMethod()) {
            response.setStatusCode(204);
            return;
        }

        chain.doFilter(request, response);
    }

    private Set<String> allowedOrigins() {
        Stream<String> localOrigin = localUrlHolder.localUrlIsSet()
                ? Stream.of(localUrlHolder.getUrl())
                : Stream.empty();
        return Stream.concat(localOrigin, configuredAllowedOrigins.stream())
                .map(this::normalizeOrigin)
                .collect(Collectors.toUnmodifiableSet());
    }

    private String normalizeOrigin(String value) {
        URI uri = URI.create(value);
        String scheme = uri.getScheme();
        String host = uri.getHost();
        int port = uri.getPort();
        if (scheme == null || host == null) {
            throw new IllegalArgumentException("Origin must include scheme and host: " + value);
        }
        return scheme + "://" + host + (port >= 0 ? ":" + port : "");
    }
}
