package one.xis.boot;

import one.xis.boot.netty.NettyServer;
import one.xis.context.AppContext;

public class XISBootRunner {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    public static void run(Class<?> applicationCLass, String[] args) {
        rejectNativeImageRuntime();
        var springCompatibility = new SpringCompatibility();
        var builder = AppContext.builder()
                .withComponentAnnotations(springCompatibility.getSpringComponentAnnotationsInClassPath().toList())
                .withBeanInitAnnotations(springCompatibility.getSpringInitAnnotationsInClassPath().toList())
                .withDependencyFieldAnnotations(springCompatibility.getSpringDependencyInjectionAnnotationsInClassPath().toList())
                .withPackage(applicationCLass.getPackage().getName())
                .withXIS();
        springCompatibility.getSpringBeanMethodAnnotationInClassPath().ifPresent(builder::withBeanMethodAnnotation);
        
        var context = builder.build();
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
            throw new IllegalStateException("XISBootRunner is the JVM runner and must not be used inside a GraalVM "
                    + "native image. Add xis-boot-native and build the application with the XIS Gradle plugin "
                    + "native tasks so the generated one.xis.boot.nativeimage.NativeRunner is used.");
        }
    }
}

    
