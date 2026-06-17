package one.xis.http.nativeimage;

import one.xis.context.AppContext;
import one.xis.http.netty.NettyServer;

/**
 * Starts a XIS HTTP Controller application from generated native component catalogs.
 * <p>
 * This runner is intended for native images and other closed-world runtimes.
 * It avoids application package scanning and uses generated direct class
 * references instead.
 */
final class XISHttpNativeRunner {

    private XISHttpNativeRunner() {
    }

    public static void run(Class<?> applicationClass, String[] args, NativeComponentRegistry... applicationRegistries) {
        var builder = new NativeAppContextBuilder()
                .withRegistry(new XisGeneratedHttpFrameworkComponents());
        builder.withRegistries(applicationRegistries);
        startServer(builder.build(), args);
    }

    /**
     * Creates a context that contains only the generated XIS HTTP Controller framework
     * components.
     * <p>
     * This method is mainly useful while bootstrapping native support. A real
     * application should pass its generated application registry via
     * {@link #run(Class, String[], NativeComponentRegistry...)}.
     */
    public static void runFrameworkOnly(String[] args) {
        var context = new NativeAppContextBuilder()
                .withRegistry(new XisGeneratedHttpFrameworkComponents())
                .build();
        startServer(context, args);
    }

    private static void startServer(AppContext context, String[] args) {
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
