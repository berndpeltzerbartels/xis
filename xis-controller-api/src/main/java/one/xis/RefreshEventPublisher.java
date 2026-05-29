package one.xis;

import java.util.Set;

/**
 * Publishes refresh events to connected browser clients.
 *
 * <p>Inject this interface into application services when server-side state
 * changes outside the current browser action and interested pages/frontlets
 * should update through the XIS event channel.</p>
 */
public interface RefreshEventPublisher {

    /**
     * Publishes the given refresh event.
     */
    void publish(RefreshEvent refreshEvent);

    /**
     * Publishes one event key to one target.
     */
    default void publish(String eventKey, RefreshTarget target) {
        publish(new RefreshEvent(eventKey, Set.of(target)));
    }

    default void publishToAll(String eventKey) {
        publish(eventKey, RefreshTarget.all());
    }

    default void publishToAllUsers(String eventKey) {
        publish(eventKey, RefreshTarget.allUsers());
    }

    default void publishToClient(String eventKey, String clientId) {
        publish(eventKey, RefreshTarget.client(clientId));
    }

    default void publishToClients(String eventKey, String... clientIds) {
        publish(eventKey, RefreshTarget.clients(clientIds));
    }

    default void publishToUser(String eventKey, String userId) {
        publish(eventKey, RefreshTarget.user(userId));
    }

    default void publishToUsers(String eventKey, String... userIds) {
        publish(eventKey, RefreshTarget.users(userIds));
    }
}
