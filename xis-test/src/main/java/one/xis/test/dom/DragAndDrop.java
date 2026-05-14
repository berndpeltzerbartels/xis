package one.xis.test.dom;

import lombok.RequiredArgsConstructor;
import one.xis.test.js.Event;

@RequiredArgsConstructor
public class DragAndDrop {

    private final Element source;
    private final Element target;

    public void doDragAndDrop() {
        if (!(source instanceof ElementImpl sourceElement) || !(target instanceof ElementImpl targetElement)) {
            throw new IllegalArgumentException("DragAndDrop expects test DOM elements.");
        }
        var dragEvent = new Event("dragstart");
        sourceElement.fireEvent("dragstart", dragEvent);
        targetElement.fireEvent("dragover", dragEvent);
        targetElement.fireEvent("drop", dragEvent);
    }
}
