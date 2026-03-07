package one.xis.ws;

import lombok.Getter;

/**
 * Server-push message that triggers a client-side update event.
 * The client will reload all pages and widgets that are annotated with
 * {@code @RefreshOnUpdateEvents} for the given event key –
 * exactly like the update-event mechanism triggered by {@code @Action(updateEventKeys=…)}.
 */
@Getter
public class WSUpdateEventMessage {

    /**
     * Discriminator field so the client can distinguish push messages from responses.
     */
    private final String responseType = "update-event";

    private final String updateEventKey;

    public WSUpdateEventMessage(String updateEventKey) {
        this.updateEventKey = updateEventKey;
    }
}
