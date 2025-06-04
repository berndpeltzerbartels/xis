package one.xis.boot.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.server.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;

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

    /**
     * Converts a RenewTokenResponse to a FullHttpResponse. Th
     *
     * @param renewTokenResponse the RenewTokenResponse to convert
     * @return a FullHttpResponse containing the JSON representation of the RenewTokenResponse
     * @throws IOException if there is an error during conversion
     */
    public FullHttpResponse toFullHttpResponse(RenewTokenResponse renewTokenResponse) throws IOException {
        long accessTokenMaxAge = renewTokenResponse.getAccessTokenExpiresAt() - Instant.now().getEpochSecond();

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NO_CONTENT
        );

        response.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.encode("access_token", renewTokenResponse.getAccessToken()) +
                        "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age=" + accessTokenMaxAge);

        response.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.encode("refresh_token", renewTokenResponse.getRenewToken()) +
                        "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age=" + Duration.ofDays(7).getSeconds());

        return response;

    }

    public FullHttpResponse toFullHttpResponse(Object obj) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(obj);
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );
    }

    public FullHttpResponse toRedirectWithCookies(String location, AuthenticationData authData) {
        long accessTokenMaxAge = authData.getAccessTokenExpiresAt() - Instant.now().getEpochSecond();

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.FOUND
        );

        response.headers().set(HttpHeaderNames.LOCATION, location);

        response.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.encode("access_token", authData.getAccessToken()) +
                        "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age=" + accessTokenMaxAge);

        response.headers().add(HttpHeaderNames.SET_COOKIE,
                ServerCookieEncoder.encode("refresh_token", authData.getRenewToken()) +
                        "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age=" + Duration.ofDays(7).getSeconds());

        return response;
    }


}
