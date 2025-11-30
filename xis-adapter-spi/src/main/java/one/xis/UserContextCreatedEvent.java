package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class UserContextCreatedEvent {
    private final UserContext userContext;
}
