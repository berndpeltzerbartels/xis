package test.page.core;

import java.util.ArrayList;
import java.util.List;

class SharedValueActionRefreshService {

    private final List<String> invocations = new ArrayList<>();
    private String value = "initial";

    String value() {
        invocations.add("value:" + value);
        return value;
    }

    void update(String value) {
        invocations.add("update:" + value);
        this.value = value;
    }

    List<String> invocations() {
        return invocations;
    }
}
