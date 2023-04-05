package one.xis.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

class DataDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        JsonNode node = mapper.readTree(jp);
        var map = mapper.treeToValue(node, Map.class);
        return map;
    }
}
