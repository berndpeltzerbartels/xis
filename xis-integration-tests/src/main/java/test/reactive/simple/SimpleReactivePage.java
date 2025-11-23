package test.reactive.simple;


import one.xis.Action;
import one.xis.Page;
import one.xis.SessionStorage;

@Page("/simpleReactive.html")
class SimpleReactivePage {
    private int counter = 1;

    @SessionStorage("counterValue")
    int count() {
        return counter;
    }

    @Action("link-clicked")
    void linkClicked() {
        counter++;
    }
}
