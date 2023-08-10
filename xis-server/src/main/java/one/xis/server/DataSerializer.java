package one.xis.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;

import java.util.Map;

/**
 * We serialize the data on our own, because the programmer
 * may configure the frameworks serialzation in a way causing problems
 * for our data.
 * <p>
 * Mainly micronaut's default serialization leaves attributes with
 * empty arrays etc.
 */
@XISComponent
class DataSerializer {

    private ObjectMapper objectMapper;

    @XISInit
    void init() {
        objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // TODO dates/timestamps
        // TODO may spring / micronaut deserialzer has to be configured, too
    }

    String serialize(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
