package one.xis.server;

import java.util.Set;

public interface RefreshEventPublisher {

    void publish(RefreshEvent refreshEvent);

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
