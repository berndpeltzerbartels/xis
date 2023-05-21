package one.xis.test.js;

import java.util.HashMap;
import java.util.Map;

public class LocalStorage {

    private final Map<String, String> items = new HashMap<>();

    public void setItem(String name, String value) {
        items.put(name, value);
    }

    public String getItem(String name) {
        return items.get(name);
    }
    
}
