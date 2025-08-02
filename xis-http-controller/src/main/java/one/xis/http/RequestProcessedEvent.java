package one.xis.http;

import lombok.Data;

@Data
public class RequestProcessedEvent {
    private final HttpRequest request;
    private final HttpResponse response;
}
