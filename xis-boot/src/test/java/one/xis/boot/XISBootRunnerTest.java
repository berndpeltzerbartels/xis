package one.xis.boot;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class XISBootRunnerTest {

    private static final String NATIVE_IMAGE_PROPERTY = "org.graalvm.nativeimage.imagecode";

    @AfterEach
    void clearNativeImageProperty() {
        System.clearProperty(NATIVE_IMAGE_PROPERTY);
    }

    @Test
    void rejectsJVMRunnerInsideNativeImage() {
        System.setProperty(NATIVE_IMAGE_PROPERTY, "runtime");

        assertThatThrownBy(XISBootRunner::rejectNativeImageRuntime)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("XISBootRunner is the JVM runner")
                .hasMessageContaining("one.xis.boot.nativeimage.NativeApp");
    }
}
