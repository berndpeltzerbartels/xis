package one.xis.test.dom;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Window {
    public final Location location;
    public History history = new History();
    private int scrollX = 0;
    private int scrollY = 0;
    private int scrollToCallCount = 0;


    public void reset() {
        location.reset();
        history.reset();
        scrollX = 0;
        scrollY = 0;
        scrollToCallCount = 0;
    }

    public void addEventListener(String type, Consumer<?> listener) {
        // No-op
    }

    public void scrollTo(int x, int y) {
        this.scrollX = x;
        this.scrollY = y;
        this.scrollToCallCount++;
    }

    public int getScrollX() {
        return scrollX;
    }

    public int getScrollY() {
        return scrollY;
    }

    public int getScrollToCallCount() {
        return scrollToCallCount;
    }
}
