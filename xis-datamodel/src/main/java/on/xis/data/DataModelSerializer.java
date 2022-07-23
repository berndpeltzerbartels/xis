package on.xis.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import one.xis.context.XISComponent;

@XISComponent
class DataModelSerializer {

    private final ObjectMapper objectMapper;

    DataModelSerializer() {
        this.objectMapper = new ObjectMapper();
    }

    String serializeToJson(DataModel model) throws JsonProcessingException {
        return objectMapper.writeValueAsString(model);
    }
}
