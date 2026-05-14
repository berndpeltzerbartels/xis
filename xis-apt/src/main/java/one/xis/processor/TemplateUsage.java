package one.xis.processor;

import java.util.LinkedHashSet;
import java.util.Set;

class TemplateUsage {

    private final Set<String> consumedDataNames = new LinkedHashSet<>();
    private final Set<String> expressionRoots = new LinkedHashSet<>();

    void consumeData(String name) {
        if (isDataName(name)) {
            consumedDataNames.add(name);
        }
    }

    void consumeExpressionRoot(String root) {
        if (isDataName(root)) {
            expressionRoots.add(root);
        }
    }

    Set<String> consumedDataNames() {
        return consumedDataNames;
    }

    Set<String> expressionRoots() {
        return expressionRoots;
    }

    boolean consumes(String name) {
        return consumedDataNames.contains(name) || expressionRoots.contains(name);
    }

    private boolean isDataName(String name) {
        return name != null && name.matches("[A-Za-z_$][A-Za-z0-9_$]*");
    }
}
