package test.page.core;

import lombok.Data;
import one.xis.Action;
import one.xis.LocalStorage;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

import java.util.ArrayList;
import java.util.List;

@Page("/shared-value-action-storage.html")
public class SharedValueActionStoragePage {

    private final List<String> invocations = new ArrayList<>();

    @SharedValue("state")
    State state(@LocalStorage("state") State state) {
        invocations.add("state");
        return state;
    }

    @ModelData("moves")
    String moves(@SharedValue("state") State state) {
        invocations.add("moves");
        return String.join(",", state.getMoves());
    }

    @Action("add")
    void add(@SharedValue("state") State state, @LocalStorage("state") State storedState) {
        invocations.add("add");
        state.getMoves().add("e2e4");
        storedState.copyFrom(state);
    }

    public List<String> getInvocations() {
        return invocations;
    }

    @Data
    public static class State {
        private List<String> moves = new ArrayList<>();

        void copyFrom(State other) {
            moves = new ArrayList<>(other.moves);
        }
    }
}
