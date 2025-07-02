package one.xis.boot.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslHandler;

class NettySchemaInjectorHandler extends ChannelInboundHandlerAdapter {

    public static final String INTERNAL_SCHEME_HEADER = "X-Internal-Scheme";

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            // Füge den Header nur hinzu, wenn er nicht bereits von einem Proxy gesetzt wurde
            if (!request.headers().contains("X-Forwarded-Proto")) {
                String scheme = (ctx.pipeline().get(SslHandler.class) != null) ? "https" : "http";
                request.headers().set(INTERNAL_SCHEME_HEADER, scheme);
            }
        }
        // Leite die Nachricht an den nächsten Handler in der Pipeline weiter
        ctx.fireChannelRead(msg);
    }
}
