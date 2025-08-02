package one.xis.http;

import lombok.Data;

@Data
public class BeforeRequestProcessingEvent {
    private final HttpRequest request;
}
