package one.xis.http.netty;

import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class NettyShutdown {

    static final long SHUTDOWN_TIMEOUT_MILLIS = 1_000;

    private final AtomicBoolean shutdownStarted = new AtomicBoolean();

    void shutdown(Channel serverChannel, EventLoopGroup bossGroup, EventLoopGroup workerGroup) {
        if (!shutdownStarted.compareAndSet(false, true)) {
            return;
        }
        try {
            closeServerChannel(serverChannel);
            shutdownGroup(workerGroup);
            shutdownGroup(bossGroup);
        } catch (Throwable throwable) {
            System.err.println("Netty shutdown failed: " + throwable.getMessage());
            throwable.printStackTrace(System.err);
        }
    }

    boolean isShutdownStarted() {
        return shutdownStarted.get();
    }

    private void closeServerChannel(Channel serverChannel) {
        if (serverChannel != null && serverChannel.isOpen()) {
            serverChannel.close().awaitUninterruptibly(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        }
    }

    private void shutdownGroup(EventLoopGroup group) {
        group.shutdownGracefully(0, SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .awaitUninterruptibly(SHUTDOWN_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    }
}
