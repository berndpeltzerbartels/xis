package one.xis.server;

import java.util.Map;

public interface DataSerializer {
    String serialize(Map<String, Object> data);
}
