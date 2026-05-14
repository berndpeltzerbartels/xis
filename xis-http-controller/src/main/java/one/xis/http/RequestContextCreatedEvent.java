package one.xis.http;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RequestContextCreatedEvent {
    private final RequestContext requestContext;
}
