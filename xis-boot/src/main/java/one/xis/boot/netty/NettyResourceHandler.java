package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.extern.java.Log;
import one.xis.context.XISComponent;

import javax.activation.MimetypesFileTypeMap;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;

@Log
@XISComponent
public class NettyResourceHandler {
    private static final String PUBLIC_RESOURCE_PATH = "/public";
    private final MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();

    public Optional<FullHttpResponse> handle(String uri) {
        return findResourceUrl(uri)
                .flatMap(this::readResourceBytes)
                .map(bytes -> createHttpResponse(bytes, uri));
    }

    private Optional<URL> findResourceUrl(String uri) {
        String normalizedUri = Path.of(uri).normalize().toString();
        if (normalizedUri.startsWith("/..")) {
            return Optional.empty(); // Path-Traversal-Versuch
        }

        String resourcePath = (PUBLIC_RESOURCE_PATH + normalizedUri).substring(1);
        URL resourceUrl = Thread.currentThread().getContextClassLoader().getResource(resourcePath);

        if (resourceUrl == null) {
            log.info("Resource not found at classpath path: " + resourcePath);
            return Optional.empty();
        }
        return Optional.of(resourceUrl);
    }

    private Optional<byte[]> readResourceBytes(URL resourceUrl) {
        try (InputStream inputStream = resourceUrl.openStream()) {
            return Optional.of(inputStream.readAllBytes());
        } catch (IOException e) {
            log.warning("Could not read resource: " + resourceUrl + ". " + e.getMessage());
            return Optional.empty();
        }
    }

    private FullHttpResponse createHttpResponse(byte[] content, String uri) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(content));
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.length);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, getContentType(uri));
        return response;
    }

    private String getContentType(String uri) {
        if (uri.endsWith(".css")) {
            return "text/css";
        }
        // Verwende den URI (z.B. /style.css) statt des vollen Classpath-Pfades
        return mimeTypesMap.getContentType(uri);
    }
}