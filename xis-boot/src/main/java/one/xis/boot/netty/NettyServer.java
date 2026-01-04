package one.xis.boot.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.context.Component;

@Component
@RequiredArgsConstructor
public class NettyServer {

    private final NettyServerHandler nettyServerHandler;
    @Setter
    @Getter
    private int port = 8080;

    public void start() throws InterruptedException {
        // Create thread factories with non-daemon threads
        DefaultThreadFactory bossThreadFactory = new DefaultThreadFactory("netty-boss", false);
        DefaultThreadFactory workerThreadFactory = new DefaultThreadFactory("netty-worker", false);
        
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1, bossThreadFactory);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(0, workerThreadFactory);

        // Add shutdown hook to gracefully stop server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server gracefully...");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }));

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(new HttpObjectAggregator(1048576));
                            ch.pipeline().addLast(new HttpServerExpectContinueHandler());
                            ch.pipeline().addLast(nettyServerHandler);
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Server started successfully on port " + port);
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}