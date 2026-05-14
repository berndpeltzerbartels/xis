package one.xis.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import one.xis.context.Bean;
import one.xis.context.Component;

import java.io.IOException;


@Component
class ObjectMapperConfig {

    @Bean
    ObjectMapper objectMapper() {
        var objectMapper = new ObjectMapper();
        var objectMapper2 = new ObjectMapper();

        // Custom serializer for java.lang.Object
        SimpleModule module = new SimpleModule();
        module.addSerializer(Object.class, new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                if (value.getClass() == Object.class) { // Only serialize exact Object instances
                    gen.writeStartObject();
                    gen.writeEndObject();
                } else {
                    objectMapper.writeValue(gen, value); // Use default serialization for other types
                }
            }
        });

        objectMapper2.registerModule(module);
        return objectMapper;
    }
}
