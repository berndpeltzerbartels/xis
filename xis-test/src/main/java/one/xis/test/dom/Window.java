package one.xis.test.dom;

import java.util.function.Consumer;

public class Window {
    public Location location = new Location();
    public History history = new History();


    public void reset() {
        location.reset();
        history.reset();
    }

    public void addEventListener(String type, Consumer<?> listener) {
        // No-op
    }
}
