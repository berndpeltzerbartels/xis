package one.xis.boot.netty;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
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
            // Path-Traversal-Angriffe durch Normalisierung verhindern
            String normalizedUri = Path.of(uri).normalize().toString();
            if (normalizedUri.startsWith("/..")) {
                return Optional.empty(); // Ungültiger Pfad
            }

            String resourcePath = PUBLIC_RESOURCE_PATH + normalizedUri;
            URL resourceUrl = getClass().getResource(resourcePath);

            if (resourceUrl == null) {
                return Optional.empty();
            }

            try (InputStream inputStream = resourceUrl.openStream()) {
                byte[] bytes = inputStream.readAllBytes();
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, Unpooled.wrappedBuffer(bytes));
                response.headers().set("Content-Type", mimeTypesMap.getContentType(normalizedUri));
                return Optional.of(response);
            }
        } catch (IOException e) {
            log.warning("Could not read resource: " + uri + ". " + e.getMessage());
            return Optional.empty();
        }
    }
}