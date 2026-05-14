package one.xis.boot.netty;

import io.netty.handler.codec.http.FullHttpRequest;

class NettyHttpUtils {

    static String getLocalUrl(FullHttpRequest request) {
        final String forwardedProto = request.headers().get("X-Forwarded-Proto");
        final String forwardedHost = request.headers().get("X-Forwarded-Host");
        final String forwardedPort = request.headers().get("X-Forwarded-Port");

        final String scheme;
        if (forwardedProto != null) {
            scheme = forwardedProto;
        } else if (request.headers().contains("X-Internal-Scheme")) {
            scheme = request.headers().get("X-Internal-Scheme");
        } else {
            scheme = "http"; // Fallback
        }

        final String host;
        if (forwardedHost != null) {
            host = forwardedHost;
        } else {
            host = request.headers().get("Host");
        }

        // Wenn X-Forwarded-Host gesetzt ist, enthält es oft schon den Port.
        // Wenn nicht, prüfen wir X-Forwarded-Port explizit.
        if (forwardedHost != null && forwardedPort != null) {
            // Verhindern, dass der Port doppelt angehängt wird.
            if (!host.contains(":")) {
                boolean isDefaultPort = ("http".equalsIgnoreCase(scheme) && "80".equals(forwardedPort)) ||
                        ("https".equalsIgnoreCase(scheme) && "443".equals(forwardedPort));
                if (!isDefaultPort) {
                    return scheme + "://" + host + ":" + forwardedPort;
                }
            }
        }

        // Fallback auf den Host-Header, der den Port enthalten kann oder auch nicht.
        return scheme + "://" + host;
    }
}