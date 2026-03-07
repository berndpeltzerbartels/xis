package one.xis.ws;

public interface RefreshEventPublisher {

    void publishRefreshEvent(RefreshEvent refreshEvent);

    void publishToAll(String eventKey);
}
