package one.xis.test.dom;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Window {
    public final Location location;
    public History history = new History();


    public void reset() {
        location.reset();
        history.reset();
    }

    public void addEventListener(String type, Consumer<?> listener) {
        // No-op
    }
}
