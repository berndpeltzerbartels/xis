package one.xis.js.connect;

import one.xis.js.Promise;
import one.xis.utils.lang.ThreeFunction;

import java.util.Map;
import java.util.function.BiFunction;

public class HttpClient {

    private final BiFunction<String, Map<String, String>, Promise> getHandler;
    private final ThreeFunction<String, Object, Map<String, String>, Promise> postHandler;

    public HttpClient(BiFunction<String, Map<String, String>, Promise> getHandler, ThreeFunction<String, Object, Map<String, String>, Promise> postHandler) {
        this.getHandler = getHandler;
        this.postHandler = postHandler;
    }

    public HttpClient() {
        this.getHandler = (a, b) -> null;
        this.postHandler = (a, b, c) -> null;
    }


    public Promise post(String uri, Object payload, Map<String, String> headers) {
        return postHandler != null ? postHandler.apply(uri, payload, headers) : null;
    }

    public Promise get(String path, Map<String, String> headers) {
        return getHandler.apply(path, headers);
    }
}
