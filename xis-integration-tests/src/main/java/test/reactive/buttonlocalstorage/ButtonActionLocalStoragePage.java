package test.reactive.buttonlocalstorage;

import lombok.Getter;
import one.xis.Action;
import one.xis.LocalStorage;
import one.xis.Page;

@Getter
@Page("/button-action-localStorage.html")
public class ButtonActionLocalStoragePage {

    private int counter = 0;

    @LocalStorage("counter")
    Integer counter() {
        return counter; // Startwert
    }

    @Action("increment-standalone")
    @LocalStorage("counter")
    Integer incrementStandalone() {
        counter += 1;
        return counter;
    }

    @Action("decrement-standalone")
    @LocalStorage("counter")
    Integer decrementStandalone() {
        counter -= 1;
        return counter;
    }
}