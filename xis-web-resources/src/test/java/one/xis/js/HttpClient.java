package one.xis.js;

import java.util.Map;

public interface HttpClient {

    Promise post(String uri, Object payload, Map<String, String> headers);

    Promise get(String path, Map<String, String> headers);
}