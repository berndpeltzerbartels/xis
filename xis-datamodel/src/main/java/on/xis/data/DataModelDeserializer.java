package on.xis.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.xis.context.XISComponent;

@XISComponent
class DataModelDeserializer {

    private final ObjectMapper objectMapper;

    DataModelDeserializer() {
        this.objectMapper = new ObjectMapper();
    }

    DataModel deserialize(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, DataModel.class);
    }
}
