package one.xis.ws;

/**
 * Response to a client-side {@code ping} message.
 * The client uses the PONG to confirm the connection is alive.
 * Must match the {@code case 'PONG'} in {@code WebsocketConnector.handleMessage()}.
 */
public class WSPongMessage {

    @SuppressWarnings("unused") // serialized to JSON
    private final String messageType = "PONG";

}
