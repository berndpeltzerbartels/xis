package one.xis.parameter;

import one.xis.context.AppContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class DefaultJsonDeserializerTest {

    private DefaultJsonDeserializer defaultJsonDeserializer;

    @BeforeEach
    void init() {
        defaultJsonDeserializer = AppContextBuilder.createInstance()
                .withSingletonClass(GsonConfig.class)
                .withSingletonClass(DefaultJsonDeserializer.class)
                .build().getSingleton(DefaultJsonDeserializer.class);
    }

    @Test
    void deserializeString() throws IOException {
        var target = new ClassTargetElement("", String.class);
        var result = defaultJsonDeserializer.deserialize("bla", target, null);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo("bla");
    }

    @Test
    void deserializeBigDecimal() throws IOException {
        var target = new ClassTargetElement("", BigDecimal.class);
        var result = defaultJsonDeserializer.deserialize("123", target, null);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(new BigDecimal("123"));
    }

    @Test
    void deserializeBoolean() throws IOException {
        var target = new ClassTargetElement("", Boolean.TYPE);
        var result = defaultJsonDeserializer.deserialize("true", target, null);
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(true);
    }
}