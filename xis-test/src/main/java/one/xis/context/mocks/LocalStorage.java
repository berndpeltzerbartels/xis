package one.xis.context.mocks;

import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class LocalStorage {
    private final Map<String, String> items = new HashMap<>();

    public void setItem(@NonNull String name, String value) {
        items.put(name, value);
    }


    public String getItem(@NonNull String name) {
        return items.get(name);
    }

}
