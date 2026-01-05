package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.java.Log;
import one.xis.context.Component;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Serves static resources from classpath /public.
 * <p>
 * Includes a small bounded LRU cache for tiny resources to reduce GC churn.
 * The cache is intentionally small to avoid memory growth on 64MB heaps.
 */
@Log
@Component
public class NettyResourceHandler {

    private static final String PUBLIC_RESOURCE_PATH = "/public";

    private static final int MAX_CACHE_ENTRIES = 128;
    private static final int MAX_CACHE_RESOURCE_BYTES = 256 * 1024; // only cache small files

    private final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

    private final Map<String, byte[]> lruCache = Collections.synchronizedMap(new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
            return size() > MAX_CACHE_ENTRIES;
        }
    });

    public Optional<FullHttpResponse> handle(String uri) {
        String normalized = normalizeUri(uri);
        if (normalized == null) {
            return Optional.empty();
        }

        return loadBytes(normalized)
                .map(bytes -> createOkResponse(bytes, normalized));
    }

    private Optional<byte[]> loadBytes(String normalizedUri) {
        byte[] cached = lruCache.get(normalizedUri);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<URL> url = findResourceUrl(normalizedUri);
        Optional<byte[]> bytes = url.flatMap(this::readResourceBytes);

        bytes.ifPresent(b -> maybeCache(normalizedUri, b));
        return bytes;
    }

    private void maybeCache(String normalizedUri, byte[] bytes) {
        if (bytes.length <= MAX_CACHE_RESOURCE_BYTES) {
            lruCache.put(normalizedUri, bytes);
        }
    }

    private Optional<URL> findResourceUrl(String normalizedUri) {
        String classpathLocation = PUBLIC_RESOURCE_PATH + normalizedUri;
        URL resourceUrl = getClass().getResource(classpathLocation);
        return Optional.ofNullable(resourceUrl);
    }

    private Optional<byte[]> readResourceBytes(URL resourceUrl) {
        try (InputStream in = resourceUrl.openStream()) {
            return Optional.of(in.readAllBytes());
        } catch (IOException e) {
            log.warning("Could not read resource: " + resourceUrl + " (" + e.getMessage() + ")");
            return Optional.empty();
        }
    }

    private FullHttpResponse createOkResponse(byte[] content, String uri) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );

        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentTypeFor(uri));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "public, max-age=3600");
        return response;
    }

    private String contentTypeFor(String uri) {
        // Some MIME maps can be weak for CSS without filename context in certain environments.
        if (uri.endsWith(".css")) {
            return "text/css";
        }
        if (uri.endsWith(".js")) {
            return "application/javascript";
        }
        return mimeTypes.getContentType(uri);
    }

    /**
     * Returns a safe, normalized uri like "/assets/app.css" or null if invalid.
     */
    private String normalizeUri(String uri) {
        if (uri == null || uri.isBlank()) {
            return null;
        }

        String u = uri.trim();

        // Strip query parameters
        int q = u.indexOf('?');
        if (q >= 0) {
            u = u.substring(0, q);
        }

        // Ensure leading slash
        if (!u.startsWith("/")) {
            u = "/" + u;
        }

        // Prevent path traversal
        if (u.contains("..")) {
            return null;
        }

        return u;
    }
}
