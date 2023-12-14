package one.xis.test.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Event {
    @Getter
    private final String eventType;

    public void preventDefault() {
    }
}
