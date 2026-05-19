package one.xis.http;

import lombok.NonNull;
import one.xis.context.DefaultComponent;

import java.net.URI;

@DefaultComponent
class CORSFilter implements HttpFilter {
    
    @Override
    public void doFilter(@NonNull HttpRequest request, @NonNull HttpResponse response, @NonNull FilterChain chain) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.isBlank()) {
            if (!isSameOriginRequest(request, origin)) {
                response.setStatusCode(403);
                return;
            }
            response.addHeader("Access-Control-Allow-Origin", origin);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            response.addHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            if (HttpMethod.OPTIONS == request.getHttpMethod()) {
                response.setStatusCode(204); // No Content
                return; // Don't continue chain for preflight
            }
        }
        chain.doFilter(request, response);
    }

    private boolean isSameOriginRequest(HttpRequest request, String origin) {
        try {
            URI originUri = URI.create(origin);
            String host = forwardedOrHostHeader(request);
            if (host == null || host.isBlank() || originUri.getScheme() == null || originUri.getHost() == null) {
                return false;
            }
            URI requestUri = URI.create(requestScheme(request) + "://" + host);
            return normalizedOrigin(originUri).equals(normalizedOrigin(requestUri));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String requestScheme(HttpRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null && !forwardedProto.isBlank()) {
            return firstForwardedValue(forwardedProto).toLowerCase();
        }
        return request.isSecure() ? "https" : "http";
    }

    private String forwardedOrHostHeader(HttpRequest request) {
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (forwardedHost != null && !forwardedHost.isBlank()) {
            return firstForwardedValue(forwardedHost);
        }
        return request.getHeader("Host");
    }

    private String firstForwardedValue(String value) {
        int comma = value.indexOf(',');
        return (comma >= 0 ? value.substring(0, comma) : value).trim();
    }

    private String normalizedOrigin(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        String host = uri.getHost().toLowerCase();
        int port = uri.getPort();
        if (port == defaultPort(scheme)) {
            port = -1;
        }
        return scheme + "://" + host + (port >= 0 ? ":" + port : "");
    }

    private int defaultPort(String scheme) {
        if ("http".equals(scheme)) {
            return 80;
        }
        if ("https".equals(scheme)) {
            return 443;
        }
        return -1;
    }
}
