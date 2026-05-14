package one.xis.boot.netty;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import lombok.RequiredArgsConstructor;
import one.xis.auth.ApiTokens;
import one.xis.auth.ApiTokensAndUrl;
import one.xis.context.Component;
import one.xis.server.ClientConfig;
import one.xis.server.ClientRequest;
import one.xis.server.ServerResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static io.netty.buffer.Unpooled.copiedBuffer;

@Component
@RequiredArgsConstructor
public class NettyMapper {

    private final ObjectMapper objectMapper;

    public ClientRequest toClientRequest(FullHttpRequest request) throws IOException {
        String json = request.content().toString(StandardCharsets.UTF_8);
        return objectMapper.readValue(json, ClientRequest.class);
    }

    public FullHttpResponse toFullHttpResponse(ServerResponse serverResponse) throws IOException {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.valueOf(serverResponse.getStatus()),
                copiedBuffer(objectMapper.writeValueAsString(serverResponse), StandardCharsets.UTF_8));
    }

    public FullHttpResponse toFullHttpResponse(ClientConfig clientConfig) throws IOException {
        String json = objectMapper.writeValueAsString(clientConfig);
        return new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, copiedBuffer(json, StandardCharsets.UTF_8));
    }

    public FullHttpResponse toFullHttpResponse(ApiTokens tokenResponse) throws IOException {
        long accessTokenMaxAge = tokenResponse.getAccessTokenExpiresIn().getSeconds();
        long renewTokenMaxAge = tokenResponse.getRenewTokenExpiresIn().getSeconds();

        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.NO_CONTENT
        );

        response.headers().add(HttpHeaderNames.SET_COOKIE, createCookie("access_token", tokenResponse.getAccessToken(), accessTokenMaxAge));
        response.headers().add(HttpHeaderNames.SET_COOKIE, createCookie("refresh_token", tokenResponse.getRenewToken(), renewTokenMaxAge));
        return response;
    }

    @SuppressWarnings("deprecation")
    private String createCookie(String name, String value, long maxAge) {
        return ServerCookieEncoder.encode(name, value) +
                "; HttpOnly; Secure; Path=/; SameSite=Strict; Max-Age=" + maxAge;
    }

    public FullHttpResponse toFullHttpResponse(String s) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(s);
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content)
        );
    }

    public FullHttpResponse toFullHttpResponse(Map<String, String> map) throws IOException {
        byte[] content = objectMapper.writeValueAsBytes(map);
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK,
                Unpooled.wrappedBuffer(content));
    }


    public FullHttpResponse toRedirectWithCookies(String location, ApiTokensAndUrl authData) {
        long accessTokenMaxAge = authData.getApiTokens().getAccessTokenExpiresIn().getSeconds();
        long renewTokenMaxAge = authData.getApiTokens().getRenewTokenExpiresIn().getSeconds();
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.FOUND
        );

        response.headers().set(HttpHeaderNames.LOCATION, location);
        response.headers().add(HttpHeaderNames.SET_COOKIE, createCookie("access_token", authData.getApiTokens().getAccessToken(), accessTokenMaxAge));
        response.headers().add(HttpHeaderNames.SET_COOKIE, createCookie("refresh_token", authData.getApiTokens().getRenewToken(), renewTokenMaxAge));
        return response;
    }


    public FullHttpResponse toErrorResponse(String message, HttpResponseStatus status) {
        return new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                status);
    }
}
