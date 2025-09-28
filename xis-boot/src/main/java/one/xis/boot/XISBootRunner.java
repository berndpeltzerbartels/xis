package one.xis.boot;

import one.xis.boot.netty.NettyServer;
import one.xis.context.AppContext;

public class XISBootRunner {

    public static void run(Class<?> applicationCLass, String[] args) {
        var context = AppContext.builder()
                .withPackage(applicationCLass.getPackage().getName())
                .withXIS()
                .build();

        NettyServer server = context.getSingleton(NettyServer.class);
        if (args != null && args.length > 0) {
            try {
                int port = Integer.parseInt(args[0]);
                server.setPort(port);
            } catch (NumberFormatException ignored) {
                throw new RuntimeException("Invalid port number: " + args[0]);
            }
        }
        System.out.println("Starting server on port " + server.getPort());
        try {
            server.start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

    