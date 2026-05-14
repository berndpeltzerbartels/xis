package test.frontlet.store.actionbutton;

import one.xis.Action;
import one.xis.SessionStorage;
import one.xis.Frontlet;


@Frontlet
class FrontletActionFrontlet {

    @Action("increment-from-frontlet")
    void incrementCounter(@SessionStorage("counter") Counter counter) {
        counter.increment(1);
    }
}