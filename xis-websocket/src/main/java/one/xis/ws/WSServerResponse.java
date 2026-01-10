package one.xis.ws;


import lombok.Data;
import lombok.EqualsAndHashCode;
import one.xis.server.ServerResponse;

/**
 * Represents a WebSocket server response, encapsulating the status code,
 * server response body, and optional location header for redirection.
 */
@Data
@EqualsAndHashCode(callSuper = true)
class WSServerResponse extends WSResponse<ServerResponse> {


    /**
     * Creates a WSServerResponse with a 200 OK status.
     */
    WSServerResponse() {
        super();
    }

    WSServerResponse(int statusCode) {
        setStatusCode(statusCode);
    }

    /**
     * Creates a WSServerResponse with a 200 OK status.
     *
     * @return A WSServerResponse instance with status code 200.
     */
    public static WSServerResponse ok() {
        return new WSServerResponse();
    }

    /**
     * Creates a WSServerResponse with a 401 Unauthorized status.
     *
     * @return A WSServerResponse instance with status code 401.
     */
    public static WSServerResponse unauthorized() {
        return new WSServerResponse(401);
    }

    /**
     * Creates a WSServerResponse with a 302 Found status and sets the Location header.
     *
     * @param location The URL to redirect to.
     * @return A WSServerResponse instance with status code 302 and the specified Location header.
     */
    public static WSServerResponse found(String location) {
        WSServerResponse response = new WSServerResponse(302);
        response.setLocationHeader(location);
        return response;
    }
}
