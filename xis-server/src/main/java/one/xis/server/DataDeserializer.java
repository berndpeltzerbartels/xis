package one.xis.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

class DataDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        ObjectNode node = mapper.readTree(jp);
        // var map = mapper.treeToValue(node, Map.class);
        var names = node.fieldNames();
        var map = new HashMap<String, String>();
        while (names.hasNext()) {
            var name = names.next();
            var valueNode = node.get(name);
            if (valueNode instanceof TextNode) {
                map.put(name, valueNode.textValue()); // otherwise, redundant quotation marks
            } else {
                map.put(name, mapper.writeValueAsString(valueNode));
            }
        }
        return map;
    }
}
