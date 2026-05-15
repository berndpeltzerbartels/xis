package one.xis.processor;

import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

class TemplateUsage {

    private final Set<String> consumedDataNames = new LinkedHashSet<>();
    private final Set<String> expressionRoots = new LinkedHashSet<>();
    private final Map<String, Integer> consumedDataLines = new LinkedHashMap<>();
    private final Map<String, Integer> expressionRootLines = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> formFieldLines = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> expressionPropertyLines = new LinkedHashMap<>();
    private final Map<String, Map<String, Integer>> localVariablePropertyLines = new LinkedHashMap<>();
    private final Map<String, TemplateDataModel> localVariableModels = new LinkedHashMap<>();
    private final List<PropertyPathUsage> propertyPaths = new ArrayList<>();

    void consumeData(String name) {
        consumeData(name, 1);
    }

    void consumeData(String name, int line) {
        if (isDataName(name)) {
            consumedDataNames.add(name);
            consumedDataLines.putIfAbsent(name, line);
        }
    }

    void consumeExpressionRoot(String root) {
        consumeExpressionRoot(root, 1);
    }

    void consumeExpressionRoot(String root, int line) {
        if (isDataName(root)) {
            expressionRoots.add(root);
            expressionRootLines.putIfAbsent(root, line);
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

    int consumedDataLine(String name) {
        return consumedDataLines.getOrDefault(name, 1);
    }

    int expressionRootLine(String name) {
        return expressionRootLines.getOrDefault(name, 1);
    }

    void consumeFormField(String formName, String fieldName, int line) {
        if (isDataName(formName) && isDataName(fieldName)) {
            formFieldLines.computeIfAbsent(formName, ignored -> new LinkedHashMap<>()).putIfAbsent(fieldName, line);
        }
    }

    Map<String, Map<String, Integer>> formFieldLines() {
        return formFieldLines;
    }

    void consumeExpressionProperty(String root, String propertyName, int line) {
        if (isDataName(root) && isDataName(propertyName)) {
            expressionPropertyLines.computeIfAbsent(root, ignored -> new LinkedHashMap<>()).putIfAbsent(propertyName, line);
        }
    }

    void consumePropertyPath(String root, List<String> properties, int line, boolean localVariable) {
        if (isDataName(root) && !properties.isEmpty()) {
            propertyPaths.add(new PropertyPathUsage(root, properties, line, localVariable));
        }
    }

    List<PropertyPathUsage> propertyPaths() {
        return propertyPaths;
    }

    Map<String, Map<String, Integer>> expressionPropertyLines() {
        return expressionPropertyLines;
    }

    void consumeLocalVariableProperty(String variableName, String propertyName, int line) {
        if (isDataName(variableName) && isDataName(propertyName)) {
            localVariablePropertyLines.computeIfAbsent(variableName, ignored -> new LinkedHashMap<>()).putIfAbsent(propertyName, line);
        }
    }

    Map<String, Map<String, Integer>> localVariablePropertyLines() {
        return localVariablePropertyLines;
    }

    void registerLocalVariable(TemplateDataModel localVariableModel) {
        if (localVariableModel != null && isDataName(localVariableModel.name())) {
            localVariableModels.putIfAbsent(localVariableModel.name(), localVariableModel);
        }
    }

    TemplateDataModel localVariable(String name) {
        return localVariableModels.get(name);
    }

    private boolean isDataName(String name) {
        return name != null && name.matches("[A-Za-z_$][A-Za-z0-9_$]*");
    }
}
