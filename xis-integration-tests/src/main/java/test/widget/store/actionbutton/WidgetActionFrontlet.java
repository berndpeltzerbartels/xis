package test.widget.store.actionbutton;

import one.xis.Action;
import one.xis.SessionStorage;
import one.xis.Frontlet;


@Frontlet
class WidgetActionFrontlet {

    @Action("increment-from-widget")
    void incrementCounter(@SessionStorage("counter") Counter counter) {
        counter.increment(1);
    }
}