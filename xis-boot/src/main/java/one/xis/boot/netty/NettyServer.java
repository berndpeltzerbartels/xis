package one.xis.boot.netty;

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
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.context.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class NettyServer {

    private static final int MAX_AGGREGATED_REQUEST_BYTES = 1 * 1024 * 1024; // 1MB
    private static final int READER_IDLE_SECONDS = 30; // close idle keep-alive connections
    private static final int WRITER_IDLE_SECONDS = 0;
    private static final int ALL_IDLE_SECONDS = 0;

    private final NettyServerHandler nettyServerHandler;

    @Setter
    @Getter
    private int port = 8080;

    public void start() throws InterruptedException {
        var bossFactory = new DefaultThreadFactory("netty-boss", false);
        var workerFactory = new DefaultThreadFactory("netty-worker", false);

        var bossGroup = new NioEventLoopGroup(1, bossFactory);
        var workerGroup = new NioEventLoopGroup(0, workerFactory);

        addShutdownHook(bossGroup, workerGroup);

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
            System.out.println("Server started on port " + port);

            bindFuture.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
            bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS);
        }
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
                ch.pipeline().addLast(new HttpObjectAggregator(MAX_AGGREGATED_REQUEST_BYTES));
                ch.pipeline().addLast(new HttpServerExpectContinueHandler());

                // Prevent endless keep-alive sockets from accumulating.
                ch.pipeline().addLast(new IdleStateHandler(
                        READER_IDLE_SECONDS, WRITER_IDLE_SECONDS, ALL_IDLE_SECONDS, TimeUnit.SECONDS
                ));
                ch.pipeline().addLast(IdleCloseHandler.INSTANCE);

                ch.pipeline().addLast(nettyServerHandler);
            }
        };
    }

    private void addShutdownHook(NioEventLoopGroup bossGroup, NioEventLoopGroup workerGroup) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }, "netty-shutdown"));
    }
}
