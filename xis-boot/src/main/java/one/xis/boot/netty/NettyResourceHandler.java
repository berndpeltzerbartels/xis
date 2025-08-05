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
        try {
            String normalizedUri = Path.of(uri).normalize().toString();
            if (normalizedUri.startsWith("/..")) {
                return Optional.empty();
            }

            String resourcePath = (PUBLIC_RESOURCE_PATH + normalizedUri).substring(1);
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resourceUrl = classLoader.getResource(resourcePath);

            if (resourceUrl == null) {
                log.info("Resource not found at classpath path: " + resourcePath);
                return Optional.empty();
            }

            try (InputStream inputStream = resourceUrl.openStream()) {
                byte[] bytes = inputStream.readAllBytes();
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));

                // KORREKTUR: Content-Type und Content-Length setzen
                if (normalizedUri.endsWith(".css")) {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/css");
                } else {
                    response.headers().set(HttpHeaderNames.CONTENT_TYPE, mimeTypesMap.getContentType(resourcePath));
                }
                response.headers().set(HttpHeaderNames.CONTENT_LENGTH, bytes.length);

                return Optional.of(response);
            }
        } catch (IOException e) {
            log.warning("Could not read resource: " + uri + ". " + e.getMessage());
            return Optional.empty();
        }
    }
}