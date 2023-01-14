package one.xis.test.mocks;

import java.util.HashMap;
import java.util.Map;

public class XISPages {

    private final Map<String, Object> pages = new HashMap<>();

    public void addPage(String key, Map<String, Object> page) {
        pages.put(key, page);
    }
}
