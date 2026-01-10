package one.xis.ws;


import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class NettyWSResponseEmitter implements WSEmitter {
    private final ChannelHandlerContext channelHandlerContext;
    private final Gson gson;

    @Override
    public void send(String responseJson) {
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame(responseJson));
    }

    @Override
    public void send(Object response) {
        send(gson.toJson(response));
    }
}
