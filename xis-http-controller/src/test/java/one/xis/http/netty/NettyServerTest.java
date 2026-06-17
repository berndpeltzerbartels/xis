package one.xis.http.netty;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NettyServerTest {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    @AfterEach
    void clearNativeImageProperty() {
        System.clearProperty(NATIVE_IMAGE_PROPERTY);
    }

    @Test
    void detectsNativeImageRuntime() {
        System.setProperty(NATIVE_IMAGE_PROPERTY, "runtime");

        assertThat(NettyServer.isNativeImageRuntime()).isTrue();
    }

    @Test
    void detectsJvmRuntime() {
        System.clearProperty(NATIVE_IMAGE_PROPERTY);

        assertThat(NettyServer.isNativeImageRuntime()).isFalse();
    }
}
