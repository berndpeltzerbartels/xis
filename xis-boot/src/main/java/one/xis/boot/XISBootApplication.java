package one.xis.boot;

import one.xis.context.AppContext;

public class XISBootApplication {

    public static void run(Class<?> applicationCLass, String[] args) throws InterruptedException {
        var context = AppContext.builder()
                .withPackage(applicationCLass.getPackage().getName())
                .withPackage(XISBootApplication.class.getPackage().getName())
                .withXIS()
                .build();

        context.getSingleton(NettyServer.class).start();
    }
}
