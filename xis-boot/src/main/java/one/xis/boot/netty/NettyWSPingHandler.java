package one.xis.boot.netty;

import io.netty.channel.ChannelHandler;

/**
 * Marker interface for WebSocket ping handlers in Netty pipeline.
 * Implementations send periodic ping frames to keep WebSocket connections alive.
 */
public interface NettyWSPingHandler extends ChannelHandler {
}
