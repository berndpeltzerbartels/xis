package one.xis.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonMapTypeAdapterIntegrationTest {
    @Test
    void testSerializeAndDeserialize() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(JsonMap.class, new JsonMapTypeAdapter())
                .create();
        JsonMap map = new JsonMap();
        map.put("string", "foo");
        map.put("number", "123");
        map.put("jsonObject", "{\"a\":1}");
        map.put("jsonArray", "[1,2,3]");
        map.put("boolean", "true");

        String json = gson.toJson(map);
        assertTrue(json.contains("\"string\":\"foo\""));
        assertTrue(json.contains("\"number\":\"123\""));
        assertTrue(json.contains("\"jsonObject\":\"{\\\"a\\\":1}\""));
        assertTrue(json.contains("\"jsonArray\":\"[1,2,3]\""));
        assertTrue(json.contains("\"boolean\":\"true\""));

        JsonMap deserialized = gson.fromJson(json, JsonMap.class);
        assertEquals(map, deserialized);
    }
}

