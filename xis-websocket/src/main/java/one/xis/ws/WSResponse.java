package one.xis.ws;

import lombok.Data;

@Data
public abstract class WSResponse<T> {

    private int statusCode;
    private T serverResponse;
    private String locationHeader;

    private final WSResponseHeaders headers = new WSResponseHeaders();

    WSResponse() {
        this.statusCode = 200;
    }

}
