package one.xis.server;

import one.xis.auth.token.DefaultUserSecurityService;
import one.xis.auth.token.UserSecurityService;
import one.xis.http.SseEmitter;
import org.junit.jupiter.api.Test;

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
        return emitter;
    }
}
