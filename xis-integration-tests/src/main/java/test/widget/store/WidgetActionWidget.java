package test.widget.store;

import one.xis.Action;
import one.xis.Widget;

@Widget
class WidgetActionWidget {
    private int counter = 5;

    //@SessionStorage("counterValue")
    int count() {
        return counter;
    }

    @Action("increment-from-widget")
    void incrementCounter() {
        counter++;
    }
}