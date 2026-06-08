package one.xis.http;

import one.xis.context.AppContext;
import one.xis.http.netty.NettyServer;

public class XISHttpRunner {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    private XISHttpRunner() {
    }

    public static void run(Class<?> applicationClass, String[] args) {
        rejectNativeImageRuntime();
        var context = AppContext.builder()
                .withPackage(applicationClass.getPackage().getName())
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

    static void rejectNativeImageRuntime() {
        if (System.getProperty(NATIVE_IMAGE_PROPERTY) != null) {
            throw new IllegalStateException("XISHttpRunner is the JVM runner and must not be used inside a GraalVM "
                    + "native image.");
        }
    }
}
