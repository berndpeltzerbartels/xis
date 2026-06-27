package one.xis.http.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultPromise;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.UploadConfiguration;
import one.xis.context.Component;
import one.xis.context.Inject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class NettyServer {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";
    private static final int READER_IDLE_SECONDS = 30; // close idle keep-alive connections
    private static final int WRITER_IDLE_SECONDS = 0;
    private static final int ALL_IDLE_SECONDS = 0;
    @Inject
    private final NettyHttpServerHandler httpServerHandler;
    private final UploadConfiguration uploadConfiguration;

    @Setter
    @Getter
    private int port = 8080;
    private final AtomicReference<io.netty.channel.Channel> serverChannelRef = new AtomicReference<>();
    private volatile NioEventLoopGroup bossGroup;
    private volatile NioEventLoopGroup workerGroup;
    private volatile NettyShutdown shutdown;
    private volatile Thread shutdownHook;

    public void start() throws InterruptedException {
        preloadNettyShutdownClasses();

        var bossFactory = new DefaultThreadFactory("netty-boss", false);
        var workerFactory = new DefaultThreadFactory("netty-worker", false);

        bossGroup = new NioEventLoopGroup(1, bossFactory);
        workerGroup = new NioEventLoopGroup(0, workerFactory);
        shutdown = new NettyShutdown();

        addShutdownHook();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true) // TCP keepalive (NOT HTTP keep-alive)
                    .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(32 * 1024, 64 * 1024))
                    .childHandler(newChannelInitializer());

            ChannelFuture bindFuture = bootstrap.bind(port).sync();
            serverChannelRef.set(bindFuture.channel());
            System.out.println("Server started on port " + port);

            bindFuture.channel().closeFuture().sync();
        } finally {
            stop();
            removeShutdownHook();
        }
    }

    public void stop() {
        var currentShutdown = shutdown;
        var currentBossGroup = bossGroup;
        var currentWorkerGroup = workerGroup;
        if (currentShutdown == null || currentBossGroup == null || currentWorkerGroup == null) {
            return;
        }
        currentShutdown.shutdown(serverChannelRef.get(), currentBossGroup, currentWorkerGroup);
    }

    private ChannelInitializer<SocketChannel> newChannelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new HttpServerCodec(
                        4096,   // maxInitialLineLength
                        8192,   // maxHeaderSize
                        8192    // maxChunkSize
                ));
                ch.pipeline().addLast(new HttpObjectAggregator(maxAggregatedRequestBytes()));
                ch.pipeline().addLast(new HttpServerExpectContinueHandler());

                // Idle detection BEFORE the HTTP handler
                ch.pipeline().addLast(new IdleStateHandler(
                        READER_IDLE_SECONDS, WRITER_IDLE_SECONDS, ALL_IDLE_SECONDS, TimeUnit.SECONDS
                ));
                ch.pipeline().addLast(IdleCloseHandler.INSTANCE);
                ch.pipeline().addLast(httpServerHandler);
            }
        };
    }

    private int maxAggregatedRequestBytes() {
        long configured = uploadConfiguration.getMaxRequestSize();
        return configured > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) configured;
    }

    private void preloadNettyShutdownClasses() {
        if (isNativeImageRuntime()) {
            // Native images cannot resolve these anonymous Netty helper classes via Class.forName
            // unless they are registered for reflection. Direct Netty bytecode references remain reachable.
            return;
        }
        // Netty loads DefaultPromise listener helpers lazily during shutdown.
        // When a running executable jar is replaced by a rebuild, late class loading can fail.
        for (var className : new String[]{
                DefaultPromise.class.getName() + "$1",
                DefaultPromise.class.getName() + "$2",
                DefaultPromise.class.getName() + "$3",
                DefaultPromise.class.getName() + "$4"
        }) {
            try {
                Class.forName(className, true, DefaultPromise.class.getClassLoader());
            } catch (ClassNotFoundException exception) {
                throw new IllegalStateException("Required Netty shutdown class is missing: " + className, exception);
            }
        }
    }

    static boolean isNativeImageRuntime() {
        return System.getProperty(NATIVE_IMAGE_PROPERTY) != null;
    }

    private void addShutdownHook() {
        shutdownHook = new Thread(() -> {
            if (shutdown == null || !shutdown.isShutdownStarted()) {
                System.out.println("Shutting down server...");
            }
            stop();
        }, "netty-shutdown");
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void removeShutdownHook() {
        var hook = shutdownHook;
        if (hook == null) {
            return;
        }
        try {
            Runtime.getRuntime().removeShutdownHook(hook);
        } catch (IllegalStateException ignored) {
            // The JVM is already running shutdown hooks.
        } finally {
            shutdownHook = null;
        }
    }
}
