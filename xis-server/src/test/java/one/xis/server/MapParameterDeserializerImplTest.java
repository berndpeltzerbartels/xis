package one.xis.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapParameterDeserializerImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize() throws JsonProcessingException {
        var json = "{ \"data\": {\"a\":1, \"b\":\"xyz\"}, \"clientId\":\"\",  \"userId\":\"\", \"action\":\"\", \"pageId\":\"\", \"widgetId\":\"\"}";
        var request = objectMapper.readValue(json, ClientRequest.class);

        assertThat(request.getData().get("a")).isEqualTo("1");
        assertThat(request.getData().get("b")).isEqualTo("xyz");

    }

}