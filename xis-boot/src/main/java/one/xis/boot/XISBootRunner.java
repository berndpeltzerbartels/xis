package one.xis.boot;

import one.xis.boot.netty.NettyServer;
import one.xis.context.AppContext;

public class XISBootRunner {

    public static void run(Class<?> applicationCLass, String[] args) {
        var context = AppContext.builder()
                .withPackage(applicationCLass.getPackage().getName())
                .withXIS()
                .build();

        try {
            context.getSingleton(NettyServer.class).start();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

