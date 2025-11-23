package test.reactive.button;

import one.xis.Action;
import one.xis.Page;
import one.xis.SessionStorage;

@Page("/button-test.html")
public class ButtonActionTestPage {

    private int counterValue = 0;

    @SessionStorage("counterValue")
    public int getCounterValue() {
        return counterValue;
    }

    @Action("increment-standalone")
    public void incrementStandalone() {
        counterValue += 5;
    }

    @Action("decrement-standalone")
    public void decrementStandalone() {
        counterValue -= 3;
    }
}