package one.xis.test.js;

import java.util.HashMap;
import java.util.Map;

public class SessionStorage {


    private final Map<String, String> items = new HashMap<>();

    public void setItem(String name, String value) {
        items.put(name, value);
    }

    public String getItem(String name) {
        return items.get(name);
    }

    public void removeItem(String name) {
        items.remove(name);
    }


    public void reset() {
        items.clear();
    }

}
