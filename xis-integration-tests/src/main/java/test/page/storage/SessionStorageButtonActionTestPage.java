package test.page.storage;

import one.xis.Action;
import one.xis.Page;

@Page("/button-test.html")
public class SessionStorageButtonActionTestPage {

    private int counterValue = 0;

    //@SessionStorage("counterValue")
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