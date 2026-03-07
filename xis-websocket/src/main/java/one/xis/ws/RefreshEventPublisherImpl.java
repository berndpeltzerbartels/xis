package one.xis.ws;


import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Component
@RequiredArgsConstructor
class RefreshEventPublisherImpl implements RefreshEventPublisher {
    private final WSService wsService;

    @Override
    public void publishRefreshEvent(RefreshEvent refreshEvent) {
        wsService.broadcastUpdateEvent(refreshEvent);
    }

    @Override
    public void publishToAll(String eventKey) {
        wsService.broadcastToAllClients(eventKey);
    }
}
