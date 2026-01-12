package one.xis.ws;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class WSResponse<T> {

    private int status;
    private T body;
    private Long messageId;

    private final Map<String, String> headers = new HashMap<>();

    public WSResponse() {
        this.status = 200;
    }

}
