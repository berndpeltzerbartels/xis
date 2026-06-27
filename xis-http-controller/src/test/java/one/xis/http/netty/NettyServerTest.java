package one.xis.http.netty;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import one.xis.UploadConfiguration;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void stopClosesStartedServerPromptly() throws Exception {
        NettyServer server = new NettyServer(mock(NettyHttpServerHandler.class), uploadConfiguration());
        server.setPort(freePort());
        Thread serverThread = new Thread(() -> {
            try {
                server.start();
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }, "netty-server-test");

        serverThread.start();
        waitForTcpPort(server.getPort());

        assertThatCode(server::stop).doesNotThrowAnyException();
        serverThread.join(2_500);

        assertThat(serverThread.isAlive()).isFalse();
    }

    private UploadConfiguration uploadConfiguration() {
        UploadConfiguration uploadConfiguration = mock(UploadConfiguration.class);
        when(uploadConfiguration.getMaxRequestSize()).thenReturn(1024L);
        return uploadConfiguration;
    }

    private int freePort() throws IOException {
        try (var socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private void waitForTcpPort(int port) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            try (var socket = new Socket()) {
                socket.connect(new InetSocketAddress("localhost", port), 200);
                return;
            } catch (IOException ignored) {
                Thread.sleep(50);
            }
        }
        throw new AssertionError("Netty server did not open port " + port);
    }
}
