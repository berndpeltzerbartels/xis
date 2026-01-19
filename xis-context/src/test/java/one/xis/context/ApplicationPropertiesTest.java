package one.xis.context;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationPropertiesTest {

    @Test
    void loadsBaseProperties() {
        Map<String, String> properties = ApplicationProperties.getAllProperties();
        
        assertThat(properties).containsEntry("test.string", "Hello World");
        assertThat(properties).containsEntry("test.int", "42");
        assertThat(properties).containsEntry("app.name", "TestApp");
    }

    @Test
    void getPropertyReturnsValue() {
        String value = ApplicationProperties.getProperty("test.string");
        
        assertThat(value).isEqualTo("Hello World");
    }

    @Test
    void getPropertyWithDefaultReturnsValue() {
        String value = ApplicationProperties.getProperty("test.string", "default");
        
        assertThat(value).isEqualTo("Hello World");
    }

    @Test
    void getPropertyWithDefaultReturnsDefaultForMissing() {
        String value = ApplicationProperties.getProperty("non.existent", "default-value");
        
        assertThat(value).isEqualTo("default-value");
    }

    @Test
    void getPropertyReturnsNullForMissing() {
        String value = ApplicationProperties.getProperty("non.existent.key");
        
        assertThat(value).isNull();
    }
}
