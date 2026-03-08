package one.xis.ws;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * @see one.xis.server.ClientRequest
 */
@RequiredArgsConstructor
enum WSRequestType {
    CONNECT("connect"),
    RECONNECT("reconnect"),
    PING("ping"),
    PUSH_ACK("push-ack"),
    CLIENT_REQUEST("client-request");
    private final String value;

    static WSRequestType fromValue(String value) {
        return Arrays.stream(WSRequestType.values())
                .filter(type -> type.value.equalsIgnoreCase(value)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("unknown request-type: " + value));
    }

}
