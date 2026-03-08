package one.xis.ws;

import lombok.Getter;

import java.util.UUID;

/**
 * Server-push message that triggers a client-side update event.
 * The client reloads all pages/widgets annotated with
 * {@code @RefreshOnUpdateEvents} for the given event key –
 * exactly like the update-event mechanism triggered by {@code @Action(updateEventKeys=…)}.
 * <p>
 * The client sends a {@code push-ack} message with the {@code eventId} back to confirm delivery.
 */
@Getter
public class WSUpdateEventMessage {

    /**
     * Discriminator so the client can distinguish push messages from request responses.
     * Must match the {@code case 'PUSH'} in {@code WebsocketConnector.handleMessage()}.
     */
    private final String messageType = "PUSH";

    private final String eventId = UUID.randomUUID().toString();

    private final String updateEventKey;

    public WSUpdateEventMessage(String updateEventKey) {
        this.updateEventKey = updateEventKey;
    }
}
