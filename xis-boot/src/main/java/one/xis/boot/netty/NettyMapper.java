package one.xis.boot.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.ClientConfig;
import one.xis.server.ClientRequest;
import one.xis.server.ServerResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static io.netty.buffer.Unpooled.copiedBuffer;


@XISComponent
@RequiredArgsConstructor
public class NettyMapper {

    private final ObjectMapper objectMapper;

    public ClientRequest toClientRequest(FullHttpRequest request) throws IOException {
        String json = request.content().toString(StandardCharsets.UTF_8);
        return objectMapper.readValue(json, ClientRequest.class);
    }

    public FullHttpResponse toFullHttpResponse(ServerResponse serverResponse) throws IOException {
        String json = objectMapper.writeValueAsString(serverResponse);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(serverResponse.getStatus()), copiedBuffer(json, StandardCharsets.UTF_8));
    }

    public FullHttpResponse toFullHttpResponse(ClientConfig clientConfig) throws IOException {
        String json = objectMapper.writeValueAsString(clientConfig);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer(json, StandardCharsets.UTF_8));
    }

    public FullHttpResponse toFullHttpResponse(Object obj) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(obj);
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );
    }


}
