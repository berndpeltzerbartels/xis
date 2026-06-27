package one.xis.http.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.Future;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NettyShutdownTest {

    @Test
    void shutsNettyDownOnlyOnce() {
        Channel channel = openChannel();
        EventLoopGroup bossGroup = eventLoopGroup();
        EventLoopGroup workerGroup = eventLoopGroup();
        NettyShutdown shutdown = new NettyShutdown();

        shutdown.shutdown(channel, bossGroup, workerGroup);
        shutdown.shutdown(channel, bossGroup, workerGroup);

        assertThat(shutdown.isShutdownStarted()).isTrue();
        verify(channel, times(1)).close();
        verify(workerGroup, times(1)).shutdownGracefully(
                eq(0L), eq(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS), eq(TimeUnit.MILLISECONDS)
        );
        verify(bossGroup, times(1)).shutdownGracefully(
                eq(0L), eq(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS), eq(TimeUnit.MILLISECONDS)
        );
    }

    @Test
    void closesServerChannelBeforeStoppingEventLoops() {
        Channel channel = openChannel();
        EventLoopGroup bossGroup = eventLoopGroup();
        EventLoopGroup workerGroup = eventLoopGroup();
        NettyShutdown shutdown = new NettyShutdown();

        shutdown.shutdown(channel, bossGroup, workerGroup);

        var inOrder = inOrder(channel, workerGroup, bossGroup);
        inOrder.verify(channel).close();
        inOrder.verify(workerGroup).shutdownGracefully(
                eq(0L), eq(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS), eq(TimeUnit.MILLISECONDS)
        );
        inOrder.verify(bossGroup).shutdownGracefully(
                eq(0L), eq(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS), eq(TimeUnit.MILLISECONDS)
        );
    }

    private Channel openChannel() {
        Channel channel = mock(Channel.class);
        ChannelFuture closeFuture = mock(ChannelFuture.class);
        when(channel.isOpen()).thenReturn(true);
        when(channel.close()).thenReturn(closeFuture);
        when(closeFuture.awaitUninterruptibly(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                .thenReturn(true);
        return channel;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private EventLoopGroup eventLoopGroup() {
        EventLoopGroup group = mock(EventLoopGroup.class);
        Future future = mock(Future.class);
        doReturn(future).when(group)
                .shutdownGracefully(0, NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        when(future.awaitUninterruptibly(NettyShutdown.SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS))
                .thenReturn(true);
        return group;
    }
}
