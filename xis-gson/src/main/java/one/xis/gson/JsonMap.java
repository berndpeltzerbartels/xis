package one.xis.gson;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsonMap extends LinkedHashMap<String, String> {

    public JsonMap() {
        super();
    }

    public JsonMap(int initialCapacity) {
        super(initialCapacity);
    }

    public JsonMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public JsonMap(Map<String, String> map) {
        super(map);
    }

    @Override
    public String toString() {
        return "JsonMap" + super.toString();
    }

    public static JsonMap of(String key, String json) {
        JsonMap map = new JsonMap();
        map.put(key, json);
        return map;
    }

    public static JsonMap of(String key1, String json1, String key2, String json2) {
        JsonMap map = new JsonMap();
        map.put(key1, json1);
        map.put(key2, json2);
        return map;
    }
}
