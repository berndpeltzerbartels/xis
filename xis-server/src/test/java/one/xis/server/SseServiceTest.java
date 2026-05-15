package one.xis.server;

import one.xis.auth.token.DefaultUserSecurityService;
import one.xis.auth.token.UserSecurityService;
import one.xis.RefreshEvent;
import one.xis.RefreshTarget;
import one.xis.http.SseEmitter;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class SseServiceTest {

    @Test
    void publishToAllSendsEventToAllOpenEmitters() {
        var service = new SseService(mock(UserSecurityService.class));
        var emitter1 = openEmitter();
        var emitter2 = openEmitter();

        service.registerEmitter("client-1", null, emitter1);
        service.registerEmitter("client-2", null, emitter2);

        service.publish(RefreshEvent.toAll("score-updated"));

        verify(emitter1).send("data:score-updated\n\n");
        verify(emitter2).send("data:score-updated\n\n");
    }

    @Test
    void publishToAllQueuesEventForTemporarilyDisconnectedKnownClient() {
        var service = new SseService(mock(UserSecurityService.class));
        var disconnectedEmitter = openEmitter();
        var activeEmitter = openEmitter();
        var reconnectedEmitter = openEmitter();

        service.registerEmitter("client-1", null, disconnectedEmitter);
        service.registerEmitter("client-2", null, activeEmitter);
        service.unregisterEmitter("client-1", disconnectedEmitter);

        service.publish(RefreshEvent.toAll("score-updated"));
        service.registerEmitter("client-1", null, reconnectedEmitter);

        verify(activeEmitter).send("data:score-updated\n\n");
        verify(disconnectedEmitter, never()).send(anyString());
        verify(reconnectedEmitter).send("data:score-updated\n\n");
    }

    @Test
    void publishToUserSendsEventToAllClientsOfUser() {
        var service = new SseService(mock(UserSecurityService.class));
        var emitter1 = openEmitter();
        var emitter2 = openEmitter();
        var emitterOther = openEmitter();

        service.registerEmitter("client-1", "user-1", emitter1);
        service.registerEmitter("client-2", "user-1", emitter2);
        service.registerEmitter("client-3", "user-2", emitterOther);

        service.publish(new RefreshEvent("score-updated", Set.of(RefreshTarget.user("user-1"))));

        verify(emitter1).send("data:score-updated\n\n");
        verify(emitter2).send("data:score-updated\n\n");
        verify(emitterOther, never()).send(anyString());
    }

    @Test
    void publishToUserQueuesEventForTemporarilyDisconnectedKnownUserClient() {
        var service = new SseService(mock(UserSecurityService.class));
        var disconnectedEmitter = openEmitter();
        var reconnectedEmitter = openEmitter();

        service.registerEmitter("client-1", "user-1", disconnectedEmitter);
        service.unregisterEmitter("client-1", disconnectedEmitter);

        service.publish(new RefreshEvent("score-updated", Set.of(RefreshTarget.user("user-1"))));
        service.registerEmitter("client-1", "user-1", reconnectedEmitter);

        verify(disconnectedEmitter, never()).send(anyString());
        verify(reconnectedEmitter).send("data:score-updated\n\n");
    }

    @Test
    void publishToClientSendsOnlyToThatClientEvenWhenUserHasSeveralClients() {
        var service = new SseService(mock(UserSecurityService.class));
        var emitter1 = openEmitter();
        var emitter2 = openEmitter();
        var emitterOther = openEmitter();

        service.registerEmitter("client-1", "user-1", emitter1);
        service.registerEmitter("client-2", "user-1", emitter2);
        service.registerEmitter("client-3", "user-2", emitterOther);

        service.publishToClient("score-updated", "client-2");

        verify(emitter1, never()).send(anyString());
        verify(emitter2).send("data:score-updated\n\n");
        verify(emitterOther, never()).send(anyString());
    }

    @Test
    void registerEmitterKeepsExistingEmitterForSameClientIdOpen() {
        var service = new SseService(mock(UserSecurityService.class));
        var firstWindowEmitter = openEmitter();
        var secondWindowEmitter = openEmitter();

        service.registerEmitter("client-1", "user-1", firstWindowEmitter);
        service.registerEmitter("client-1", "user-1", secondWindowEmitter);

        service.publishToClient("score-updated", "client-1");

        verify(firstWindowEmitter, never()).close();
        verify(secondWindowEmitter, never()).close();
        verify(firstWindowEmitter).send("data:score-updated\n\n");
        verify(secondWindowEmitter).send("data:score-updated\n\n");
    }

    @Test
    void unregisterEmitterRemovesOnlyThatEmitterForSameClientId() {
        var service = new SseService(mock(UserSecurityService.class));
        var closedWindowEmitter = openEmitter();
        var remainingWindowEmitter = openEmitter();

        service.registerEmitter("client-1", "user-1", closedWindowEmitter);
        service.registerEmitter("client-1", "user-1", remainingWindowEmitter);
        service.unregisterEmitter("client-1", closedWindowEmitter);

        service.publishToClient("score-updated", "client-1");

        verify(closedWindowEmitter, never()).send(anyString());
        verify(remainingWindowEmitter).send("data:score-updated\n\n");
    }

    @Test
    void pendingClientEventIsFlushedOnceWhenSeveralEmittersReconnect() {
        var service = new SseService(mock(UserSecurityService.class));
        var firstWindowEmitter = openEmitter();
        var secondWindowEmitter = openEmitter();

        service.publishToClient("score-updated", "client-1");
        service.registerEmitter("client-1", "user-1", firstWindowEmitter);
        service.registerEmitter("client-1", "user-1", secondWindowEmitter);

        verify(firstWindowEmitter).send("data:score-updated\n\n");
        verify(secondWindowEmitter, never()).send(anyString());
    }

    @Test
    void publishToClientQueuesEventUntilClientReconnects() {
        var service = new SseService(mock(UserSecurityService.class));
        var emitter = openEmitter();

        service.publishToClient("score-updated", "client-1");
        service.registerEmitter("client-1", null, emitter);

        verify(emitter).send("data:score-updated\n\n");
    }

    @Test
    void publishToClientQueuesEventWhenEmitterIsClosed() {
        var service = new SseService(mock(UserSecurityService.class));
        var closedEmitter = mock(SseEmitter.class);
        when(closedEmitter.isOpen()).thenReturn(false);
        var reconnectedEmitter = openEmitter();

        service.registerEmitter("client-1", null, closedEmitter);
        service.publishToClient("score-updated", "client-1");
        service.registerEmitter("client-1", null, reconnectedEmitter);

        verify(closedEmitter, never()).send(anyString());
        verify(reconnectedEmitter).send("data:score-updated\n\n");
    }

    @Test
    void publishToClientQueuesEventWhenSendFails() {
        var service = new SseService(mock(UserSecurityService.class));
        var failingEmitter = failingEmitter();
        var reconnectedEmitter = openEmitter();

        service.registerEmitter("client-1", null, failingEmitter);
        service.publishToClient("score-updated", "client-1");
        service.registerEmitter("client-1", null, reconnectedEmitter);

        verify(failingEmitter).send("data:score-updated\n\n");
        verify(failingEmitter).close();
        verify(reconnectedEmitter).send("data:score-updated\n\n");
    }

    @Test
    void flushPendingEventsKeepsEventWhenSendFails() {
        var service = new SseService(mock(UserSecurityService.class));
        var failingEmitter = failingEmitter();
        var reconnectedEmitter = openEmitter();

        service.publishToClient("score-updated", "client-1");
        service.registerEmitter("client-1", null, failingEmitter);
        service.registerEmitter("client-1", null, reconnectedEmitter);

        verify(failingEmitter).send("data:score-updated\n\n");
        verify(failingEmitter).close();
        verify(reconnectedEmitter).send("data:score-updated\n\n");
    }

    @Test
    void publishToAllQueuesFailedSendOnlyOnce() {
        var service = new SseService(mock(UserSecurityService.class));
        var failingEmitter = failingEmitter();
        var reconnectedEmitter = openEmitter();

        service.registerEmitter("client-1", null, failingEmitter);
        service.publish(RefreshEvent.toAll("score-updated"));
        service.registerEmitter("client-1", null, reconnectedEmitter);

        verify(failingEmitter).send("data:score-updated\n\n");
        verify(reconnectedEmitter, times(1)).send("data:score-updated\n\n");
    }

    @Test
    void publishToAllUsersSendsOnlyToAuthenticatedClients() {
        var service = new SseService(mock(UserSecurityService.class));
        var emitter1 = openEmitter();
        var emitter2 = openEmitter();
        var emitterAnonymous = openEmitter();

        service.registerEmitter("client-1", "user-1", emitter1);
        service.registerEmitter("client-2", "user-2", emitter2);
        service.registerEmitter("client-3", null, emitterAnonymous);

        service.publish("score-updated", RefreshTarget.allUsers());

        verify(emitter1).send("data:score-updated\n\n");
        verify(emitter2).send("data:score-updated\n\n");
        verify(emitterAnonymous, never()).send(anyString());
    }

    @Test
    void publishToUserFailsWithoutAuthenticationModule() {
        var service = new SseService(new DefaultUserSecurityService());

        assertThatThrownBy(() -> service.publish(new RefreshEvent("score-updated", Set.of(RefreshTarget.user("user-1")))))
                .isInstanceOf(UserTargetingNotAvailableException.class)
                .hasMessageContaining("xis-authentication");
    }

    @Test
    void publishToAllUsersFailsWithoutAuthenticationModule() {
        var service = new SseService(new DefaultUserSecurityService());

        assertThatThrownBy(() -> service.publish("score-updated", RefreshTarget.allUsers()))
                .isInstanceOf(UserTargetingNotAvailableException.class)
                .hasMessageContaining("xis-authentication");
    }

    private SseEmitter openEmitter() {
        var emitter = mock(SseEmitter.class);
        when(emitter.isOpen()).thenReturn(true);
        when(emitter.send(anyString())).thenReturn(CompletableFuture.completedFuture(null));
        return emitter;
    }

    private SseEmitter failingEmitter() {
        var emitter = mock(SseEmitter.class);
        when(emitter.isOpen()).thenReturn(true);
        when(emitter.send(anyString())).thenReturn(CompletableFuture.failedFuture(new RuntimeException("connection lost")));
        return emitter;
    }
}
