package test.widget.global;


import one.xis.Action;
import one.xis.GlobalVariable;
import one.xis.Page;

@Page("/simpleReactive.html")
class SimpleGlobalPage {
    private int counter = 1;

    @GlobalVariable("counterValue")
    int count() {
        return counter;
    }

    @Action("link-clicked")
    void linkClicked() {
        counter++;
    }
}
