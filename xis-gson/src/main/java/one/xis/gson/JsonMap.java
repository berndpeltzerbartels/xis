package one.xis.gson;

import java.util.HashMap;
import java.util.LinkedHashMap;

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

    public JsonMap(HashMap<String, String> map) {
        super(map);
    }

    @Override
    public String toString() {
        return "JsonMap" + super.toString();
    }
}
