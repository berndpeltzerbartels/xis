package test.page.modelcalls.actionstorageinvocation;

import one.xis.context.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ActionStorageInvocationCounter {
    private final List<String> invocations = new ArrayList<>();

    void add(String invocation) {
        invocations.add(invocation);
    }

    public List<String> getInvocations() {
        return invocations;
    }
}
