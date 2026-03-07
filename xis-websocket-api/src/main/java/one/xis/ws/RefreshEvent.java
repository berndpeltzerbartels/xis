package one.xis.ws;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

@Getter
@SuppressWarnings("unused")
@RequiredArgsConstructor
public class RefreshEvent {
    private final String eventKey;
    private final Collection<String> clientIds = new HashSet<>();
    private final Collection<String> userIds = new HashSet<>();

    public static RefreshEvent forClientId(String eventKey, String clientId) {
        RefreshEvent refreshEvent = new RefreshEvent(eventKey);
        refreshEvent.addClientId(clientId);
        return refreshEvent;
    }

    public static RefreshEvent forUserId(String eventKey, String userId) {
        RefreshEvent refreshEvent = new RefreshEvent(eventKey);
        refreshEvent.addUserId(userId);
        return refreshEvent;
    }


    public RefreshEvent addClientId(String clientId) {
        this.clientIds.add(clientId);
        return this;
    }

    public RefreshEvent addUserId(String userId) {
        this.userIds.add(userId);
        return this;
    }

    public RefreshEvent addClientIds(Collection<String> clientIds) {
        this.clientIds.addAll(clientIds);
        return this;
    }

    public RefreshEvent addClientIds(String... clientIds) {
        this.clientIds.addAll(Arrays.asList(clientIds));
        return this;
    }

    public RefreshEvent addUserIds(Collection<String> userIds) {
        this.userIds.addAll(userIds);
        return this;
    }

    public RefreshEvent addUserIds(String... userIds) {
        this.userIds.addAll(Arrays.asList(userIds));
        return this;
    }


}
