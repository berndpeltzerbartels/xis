package test.frontlet.store.actionbutton;

import lombok.Data;

@Data
class Counter {
    private int value;

    void increment(int by) {
        value += by;
    }
}
