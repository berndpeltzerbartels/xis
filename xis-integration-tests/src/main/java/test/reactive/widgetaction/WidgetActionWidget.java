package test.reactive.widgetaction;

import one.xis.Action;
import one.xis.ClientState;
import one.xis.Widget;

@Widget
class WidgetActionWidget {
    private int counter = 5;

    @ClientState("counterValue")
    int count() {
        return counter;
    }

    @Action("increment-from-widget")
    void incrementCounter() {
        counter++;
    }
}