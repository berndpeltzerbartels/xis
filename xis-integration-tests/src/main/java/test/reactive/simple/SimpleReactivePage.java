package test.reactive.simple;


import one.xis.Action;
import one.xis.ClientState;
import one.xis.Page;

@Page("/simpleReactive.html")
class SimpleReactivePage {
    private int counter = 1;

    @ClientState("counterValue")
    int count() {
        return counter;
    }

    @Action("link-clicked")
    void linkClicked() {
        counter++;
    }
}
