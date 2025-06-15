package one.xis.server;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MapParameterDeserializerImplTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void deserialize() throws JsonProcessingException {
        var json = "{ \"formData\": {\"a\":1, \"b\":\"xyz\"}, \"clientId\":\"\", \"action\":\"\", \"pageId\":\"\", \"widgetId\":\"\"}";
        var request = objectMapper.readValue(json, ClientRequest.class);

        assertThat(request.getFormData().get("a")).isEqualTo("1");
        assertThat(request.getFormData().get("b")).isEqualTo("xyz");

    }

}