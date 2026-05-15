package one.xis.processor;

import one.xis.html.HtmlParser;
import one.xis.html.document.Element;
import one.xis.html.document.Node;
import one.xis.html.document.TextNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class XisTemplateValidator {

    private static final Pattern TEXT_EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]*)}");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
    private static final String IDENTIFIER = "[A-Za-z_$][A-Za-z0-9_$]*";
    private static final Pattern PROPERTY_ACCESS_PATTERN = Pattern.compile("(?<!\\.)\\b" + IDENTIFIER + "(?:\\s*\\.\\s*" + IDENTIFIER + ")+\\b");
    private static final Set<String> IGNORED_EXPRESSION_ROOTS = Set.of(
            "true", "false", "null", "undefined", "empty", "notEmpty",
            "isUserInRole", "isUserInRoles"
    );

    private final boolean failFast;
    private final HtmlParser htmlParser = new HtmlParser();
    private final TemplateValidationRules rules = new TemplateValidationRules();

    XisTemplateValidator(boolean failFast) {
        this.failFast = failFast;
    }

    List<ValidationError> validate(Path projectDir, List<ControllerTemplateModel> controllers) {
        List<ValidationError> errors = new ArrayList<>();
        for (ControllerTemplateModel controller : controllers) {
            validateControllerTemplate(projectDir, controller, errors);
            if (failFast && !errors.isEmpty()) {
                return errors;
            }
        }
        return errors;
    }

    private void validateControllerTemplate(Path projectDir, ControllerTemplateModel controller, List<ValidationError> errors) {
        if (!Files.exists(controller.templateFile())) {
            return;
        }

        TemplateUsage usage = validateTemplate(projectDir, controller, errors);
        if (failFast && !errors.isEmpty()) {
            return;
        }
        validateControllerDataUsage(projectDir, controller, usage, errors);
    }

    private TemplateUsage validateTemplate(Path projectDir, ControllerTemplateModel controller, List<ValidationError> errors) {
        Path templateFile = controller.templateFile();
        String content = readTemplate(templateFile);
        Element rootElement = parseTemplate(projectDir, templateFile, content, errors);
        TemplateUsage usage = new TemplateUsage();
        if (rootElement == null) {
            return usage;
        }

        ValidationErrorCollector errorCollector = new ValidationErrorCollector(failFast, projectDir, templateFile, errors);
        validateElement(content, rootElement, controller, usage, errorCollector, new LinkedHashSet<>(), new HashMap<>(), null, new LineNumberResolver(content));
        return usage;
    }

    private Element parseTemplate(Path projectDir, Path templateFile, String content, List<ValidationError> errors) {
        try {
            return htmlParser.parse(content).getDocumentElement();
        } catch (RuntimeException e) {
            errors.add(ValidationError.inFile(projectDir, templateFile, 1, "Template cannot be parsed by XIS: " + e.getMessage()));
            return null;
        }
    }

    private void validateElement(String content,
                                 Element element,
                                 ControllerTemplateModel controller,
                                 TemplateUsage usage,
                                 ValidationErrorCollector errors,
                                 Set<String> localVariables,
                                 Map<String, TemplateDataModel> localVariableModels,
                                 String currentFormDataName,
                                 LineNumberResolver lineNumberResolver) {
        int line = lineNumberResolver.lineNumber(element);
        TemplateElement templateElement = new TemplateElement(element, line);
        rules.validateElement(templateElement, errors);
        collectElementUsage(element, controller, usage, localVariables, localVariableModels, line, currentFormDataName);
        if (errors.shouldStop()) {
            return;
        }

        Set<String> childLocalVariables = childLocalVariables(element, localVariables);
        Map<String, TemplateDataModel> childLocalVariableModels = childLocalVariableModels(element, controller, usage, localVariableModels);
        validateChildren(content, element, controller, usage, errors, childLocalVariables, childLocalVariableModels, line, childFormDataName(element, currentFormDataName), lineNumberResolver);
    }

    private void validateChildren(String content,
                                  Element element,
                                  ControllerTemplateModel controller,
                                  TemplateUsage usage,
                                  ValidationErrorCollector errors,
                                  Set<String> localVariables,
                                  Map<String, TemplateDataModel> localVariableModels,
                                  int parentLine,
                                  String currentFormDataName,
                                  LineNumberResolver lineNumberResolver) {
        Node child = element.getFirstChild();
        while (child != null) {
            validateChild(content, child, controller, usage, errors, localVariables, localVariableModels, parentLine, currentFormDataName, lineNumberResolver);
            if (errors.shouldStop()) {
                return;
            }
            child = child.getNextSibling();
        }
    }

    private void validateChild(String content,
                               Node child,
                               ControllerTemplateModel controller,
                               TemplateUsage usage,
                               ValidationErrorCollector errors,
                               Set<String> localVariables,
                               Map<String, TemplateDataModel> localVariableModels,
                               int parentLine,
                               String currentFormDataName,
                               LineNumberResolver lineNumberResolver) {
        if (child instanceof Element childElement) {
            validateElement(content, childElement, controller, usage, errors, localVariables, localVariableModels, currentFormDataName, lineNumberResolver);
        } else if (child instanceof TextNode textNode) {
            collectTextExpressions(textNode.getText(), usage, localVariables, localVariableModels, parentLine);
        }
    }

    private void collectElementUsage(Element element,
                                     ControllerTemplateModel controller,
                                     TemplateUsage usage,
                                     Set<String> localVariables,
                                     Map<String, TemplateDataModel> localVariableModels,
                                     int line,
                                     String currentFormDataName) {
        collectBindingUsage(element, usage, line);
        collectFieldBindingUsage(element, usage, line, currentFormDataName);
        collectAttributeExpressions(element, controller, usage, localVariables, localVariableModels, line);
    }

    private void collectBindingUsage(Element element, TemplateUsage usage, int line) {
        String binding = element.getAttributes().get("xis:binding");
        if (binding != null && "form".equals(element.getLocalName())) {
            usage.consumeData(firstPathSegment(binding), line);
        }
        String frameworkFormBinding = element.getAttributes().get("binding");
        if (frameworkFormBinding != null && "xis:form".equals(element.getLocalName())) {
            usage.consumeData(firstPathSegment(frameworkFormBinding), line);
        }
    }

    private void collectFieldBindingUsage(Element element, TemplateUsage usage, int line, String currentFormDataName) {
        if (currentFormDataName == null || isFormElement(element)) {
            return;
        }
        String fieldBinding = fieldBinding(element);
        if (fieldBinding != null) {
            usage.consumeFormField(currentFormDataName, firstPathSegment(fieldBinding), line);
        }
    }

    private String childFormDataName(Element element, String currentFormDataName) {
        String formBinding = formBinding(element);
        if (formBinding != null) {
            return firstPathSegment(formBinding);
        }
        return currentFormDataName;
    }

    private String formBinding(Element element) {
        if ("form".equals(element.getLocalName())) {
            return element.getAttributes().get("xis:binding");
        }
        if ("xis:form".equals(element.getLocalName())) {
            return element.getAttributes().get("binding");
        }
        return null;
    }

    private boolean isFormElement(Element element) {
        return "form".equals(element.getLocalName()) || "xis:form".equals(element.getLocalName());
    }

    private String fieldBinding(Element element) {
        String xisBinding = element.getAttributes().get("xis:binding");
        if (xisBinding != null) {
            return xisBinding;
        }
        return switch (element.getLocalName()) {
            case "xis:input", "xis:textarea", "xis:select", "xis:checkbox", "xis:radio" -> element.getAttributes().get("binding");
            default -> null;
        };
    }

    private void collectAttributeExpressions(Element element,
                                             ControllerTemplateModel controller,
                                             TemplateUsage usage,
                                             Set<String> localVariables,
                                             Map<String, TemplateDataModel> localVariableModels,
                                             int line) {
        Set<String> elementLocalVariables = childLocalVariables(element, localVariables);
        Map<String, TemplateDataModel> elementLocalVariableModels = childLocalVariableModels(element, controller, usage, localVariableModels);
        for (var entry : element.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            if ("xis:foreach".equals(attributeName) || "xis:repeat".equals(attributeName)) {
                collectForeachExpression(attributeValue, usage, localVariables, localVariableModels, line);
            } else if ("array".equals(attributeName) && "xis:foreach".equals(element.getLocalName())) {
                collectExpression(attributeValue, usage, localVariables, localVariableModels, line);
            } else if (attributeValue != null && attributeValue.contains("${")) {
                collectTextExpressions(attributeValue, usage, elementLocalVariables, elementLocalVariableModels, line);
            } else if ("xis:if".equals(attributeName) || ("condition".equals(attributeName) && "xis:if".equals(element.getLocalName()))) {
                collectExpression(attributeValue, usage, elementLocalVariables, elementLocalVariableModels, line);
            } else if ("xis:drag".equals(attributeName)) {
                collectDragExpression(attributeValue, usage, elementLocalVariables, elementLocalVariableModels, line);
            } else if ("xis:drop".equals(attributeName)) {
                collectDropExpression(attributeValue, usage, elementLocalVariables, elementLocalVariableModels, line);
            }
        }
    }

    private Set<String> childLocalVariables(Element element, Set<String> localVariables) {
        Set<String> childVariables = new LinkedHashSet<>(localVariables);
        String foreachVariable = foreachVariable(element);
        if (foreachVariable != null) {
            childVariables.add(foreachVariable);
            childVariables.add(foreachVariable + "Index");
        }
        return childVariables;
    }

    private Map<String, TemplateDataModel> childLocalVariableModels(Element element,
                                                                    ControllerTemplateModel controller,
                                                                    TemplateUsage usage,
                                                                    Map<String, TemplateDataModel> localVariableModels) {
        Map<String, TemplateDataModel> childVariables = new HashMap<>(localVariableModels);
        String variable = foreachVariable(element);
        if (variable != null) {
            TemplateDataModel model = localVariableModel(element, controller, variable);
            if (model != null) {
                childVariables.put(variable, model);
                usage.registerLocalVariable(model);
            }
        }
        return childVariables;
    }

    private TemplateDataModel localVariableModel(Element element, ControllerTemplateModel controller, String variable) {
        if (controller == null) {
            return null;
        }
        TemplateDataModel sourceModel = foreachSourceModel(element, controller);
        if (sourceModel == null || sourceModel.elementModel() == null) {
            return null;
        }
        TemplateDataModel elementModel = sourceModel.elementModel();
        return new TemplateDataModel(variable, elementModel.fields(), elementModel.elementModel());
    }

    private TemplateDataModel foreachSourceModel(Element element, ControllerTemplateModel controller) {
        List<String> sourcePath = foreachSourcePath(element);
        if (sourcePath.isEmpty()) {
            return null;
        }
        TemplateDataModel model = controller.modelData(sourcePath.get(0));
        for (int i = 1; model != null && i < sourcePath.size(); i++) {
            model = model.field(sourcePath.get(i));
        }
        return model;
    }

    private List<String> foreachSourcePath(Element element) {
        if ("xis:foreach".equals(element.getLocalName())) {
            return propertyPath(unwrapExpression(element.getAttributes().get("array")));
        }
        String expression = element.getAttributes().get("xis:foreach");
        if (expression == null) {
            expression = element.getAttributes().get("xis:repeat");
        }
        if (expression == null) {
            return List.of();
        }
        int separator = expression.indexOf(':');
        if (separator >= 0 && separator < expression.length() - 1) {
            return propertyPath(unwrapExpression(expression.substring(separator + 1).trim()));
        }
        return List.of();
    }

    private String foreachVariable(Element element) {
        if ("xis:foreach".equals(element.getLocalName())) {
            return element.getAttributes().get("var");
        }
        String foreach = element.getAttributes().get("xis:foreach");
        if (foreach != null) {
            return variableName(foreach);
        }
        String repeat = element.getAttributes().get("xis:repeat");
        if (repeat != null) {
            return variableName(repeat);
        }
        return null;
    }

    private String variableName(String iterationExpression) {
        int separator = iterationExpression.indexOf(':');
        if (separator > 0) {
            return iterationExpression.substring(0, separator).trim();
        }
        return null;
    }

    private void collectTextExpressions(String text,
                                        TemplateUsage usage,
                                        Set<String> localVariables,
                                        Map<String, TemplateDataModel> localVariableModels,
                                        int line) {
        Matcher matcher = TEXT_EXPRESSION_PATTERN.matcher(text);
        while (matcher.find()) {
            collectExpression(matcher.group(1), usage, localVariables, localVariableModels, line);
        }
    }

    private void collectForeachExpression(String expression,
                                          TemplateUsage usage,
                                          Set<String> localVariables,
                                          Map<String, TemplateDataModel> localVariableModels,
                                          int line) {
        int separator = expression.indexOf(':');
        if (separator >= 0 && separator < expression.length() - 1) {
            collectExpression(expression.substring(separator + 1).trim(), usage, localVariables, localVariableModels, line);
        }
    }

    private void collectDragExpression(String expression,
                                       TemplateUsage usage,
                                       Set<String> localVariables,
                                       Map<String, TemplateDataModel> localVariableModels,
                                       int line) {
        int separator = expression.indexOf(':');
        if (separator >= 0 && separator < expression.length() - 1) {
            collectExpression(expression.substring(separator + 1).trim(), usage, localVariables, localVariableModels, line);
        }
    }

    private void collectDropExpression(String expression,
                                       TemplateUsage usage,
                                       Set<String> localVariables,
                                       Map<String, TemplateDataModel> localVariableModels,
                                       int line) {
        int open = expression.indexOf('(');
        int close = expression.lastIndexOf(')');
        if (open > 0 && close > open) {
            for (String argument : splitArguments(expression.substring(open + 1, close))) {
                collectExpression(dropArgumentExpression(argument), usage, localVariables, localVariableModels, line);
            }
        }
    }

    private void collectExpression(String expression,
                                   TemplateUsage usage,
                                   Set<String> localVariables,
                                   Map<String, TemplateDataModel> localVariableModels,
                                   int line) {
        String normalized = removeStringLiterals(unwrapExpression(expression));
        collectPropertyPaths(normalized, usage, localVariables, localVariableModels, line);
        Matcher matcher = IDENTIFIER_PATTERN.matcher(normalized);
        while (matcher.find()) {
            String identifier = matcher.group();
            if (isPropertyName(normalized, matcher.start())
                    || isFunctionCall(normalized, matcher.end())
                    || isIgnoredIdentifier(identifier, localVariables)) {
                continue;
            }
            usage.consumeExpressionRoot(identifier, line);
        }
    }

    private void collectPropertyPaths(String expression,
                                      TemplateUsage usage,
                                      Set<String> localVariables,
                                      Map<String, TemplateDataModel> localVariableModels,
                                      int line) {
        Matcher matcher = PROPERTY_ACCESS_PATTERN.matcher(expression);
        while (matcher.find()) {
            List<String> path = propertyPath(matcher.group());
            if (path.size() < 2) {
                continue;
            }
            String root = path.get(0);
            if (isIgnoredIdentifier(root, Set.of())) {
                continue;
            }
            List<String> properties = path.subList(1, path.size());
            if (localVariables.contains(root)) {
                usage.consumePropertyPath(root, properties, line, true);
            } else {
                usage.consumePropertyPath(root, properties, line, false);
            }
        }
    }

    private List<String> propertyPath(String expression) {
        List<String> path = new ArrayList<>();
        for (String segment : expression.split("\\.")) {
            String trimmed = segment.trim();
            if (!trimmed.isEmpty()) {
                path.add(trimmed);
            }
        }
        return path;
    }

    private List<String> splitArguments(String source) {
        List<String> result = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();
        char quote = 0;
        int depth = 0;
        for (int index = 0; index < source.length(); index++) {
            char c = source.charAt(index);
            if (quote != 0) {
                buffer.append(c);
                if (c == quote && source.charAt(index - 1) != '\\') {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
                buffer.append(c);
            } else if (c == '(' || c == '[') {
                depth++;
                buffer.append(c);
            } else if (c == ')' || c == ']') {
                depth--;
                buffer.append(c);
            } else if (c == ',' && depth == 0) {
                result.add(buffer.toString().trim());
                buffer.setLength(0);
            } else {
                buffer.append(c);
            }
        }
        if (!buffer.isEmpty()) {
            result.add(buffer.toString().trim());
        }
        return result;
    }

    private String dropArgumentExpression(String argument) {
        int assignment = findTopLevelAssignment(argument);
        if (assignment > 0) {
            return argument.substring(assignment + 1).trim();
        }
        return argument;
    }

    private int findTopLevelAssignment(String source) {
        char quote = 0;
        int depth = 0;
        for (int index = 0; index < source.length(); index++) {
            char c = source.charAt(index);
            if (quote != 0) {
                if (c == quote && source.charAt(index - 1) != '\\') {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
            } else if (c == '(' || c == '[') {
                depth++;
            } else if (c == ')' || c == ']') {
                depth--;
            } else if (c == '=' && depth == 0 && isAssignmentOperator(source, index)) {
                return index;
            }
        }
        return -1;
    }

    private boolean isAssignmentOperator(String source, int index) {
        char before = index > 0 ? source.charAt(index - 1) : 0;
        char after = index < source.length() - 1 ? source.charAt(index + 1) : 0;
        return before != '=' && before != '!' && before != '<' && before != '>' && after != '=';
    }

    private String removeStringLiterals(String expression) {
        StringBuilder result = new StringBuilder(expression.length());
        char quote = 0;
        for (int index = 0; index < expression.length(); index++) {
            char c = expression.charAt(index);
            if (quote != 0) {
                result.append(' ');
                if (c == quote && expression.charAt(index - 1) != '\\') {
                    quote = 0;
                }
            } else if (c == '\'' || c == '"') {
                quote = c;
                result.append(' ');
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String unwrapExpression(String expression) {
        String trimmed = expression == null ? "" : expression.trim();
        if (trimmed.startsWith("${") && trimmed.endsWith("}")) {
            return trimmed.substring(2, trimmed.length() - 1);
        }
        return trimmed;
    }

    private boolean isFunctionCall(String expression, int endIndex) {
        int index = endIndex;
        while (index < expression.length() && Character.isWhitespace(expression.charAt(index))) {
            index++;
        }
        return index < expression.length() && expression.charAt(index) == '(';
    }

    private boolean isPropertyName(String expression, int startIndex) {
        int index = startIndex - 1;
        while (index >= 0 && Character.isWhitespace(expression.charAt(index))) {
            index--;
        }
        return index >= 0 && expression.charAt(index) == '.';
    }

    private boolean isIgnoredIdentifier(String identifier, Set<String> localVariables) {
        return IGNORED_EXPRESSION_ROOTS.contains(identifier) || localVariables.contains(identifier);
    }

    private String firstPathSegment(String path) {
        String expression = unwrapExpression(path);
        int dot = expression.indexOf('.');
        int bracket = expression.indexOf('[');
        int end = expression.length();
        if (dot >= 0) {
            end = Math.min(end, dot);
        }
        if (bracket >= 0) {
            end = Math.min(end, bracket);
        }
        return expression.substring(0, end);
    }

    private void validateControllerDataUsage(Path projectDir,
                                             ControllerTemplateModel controller,
                                             TemplateUsage usage,
                                             List<ValidationError> errors) {
        ValidationErrorCollector collector = new ValidationErrorCollector(failFast, projectDir, controller.templateFile(), errors);
        validateProvidedDataIsConsumed(controller, usage, collector);
        if (collector.shouldStop()) {
            return;
        }
        validateConsumedDataIsProvided(controller, usage, collector);
        if (collector.shouldStop()) {
            return;
        }
        validateExpressionRootsAreProvided(controller, usage, collector);
        if (collector.shouldStop()) {
            return;
        }
        validateFormFieldsAreProvided(controller, usage, collector);
        if (collector.shouldStop()) {
            return;
        }
        validatePropertyPathsAreProvided(controller, usage, collector);
    }

    private void validateProvidedDataIsConsumed(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (String modelDataName : controller.modelDataNames()) {
            if (!usage.consumes(modelDataName)) {
                errors.add(1, "@ModelData \"" + modelDataName + "\" is not used by the template for " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
        for (String formDataName : controller.formDataNames()) {
            if (!usage.consumes(formDataName)) {
                errors.add(1, "@FormData \"" + formDataName + "\" is not used by the template for " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private void validateExpressionRootsAreProvided(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (String expressionRoot : usage.expressionRoots()) {
            if (!controller.providesModelData(expressionRoot)) {
                errors.add(usage.expressionRootLine(expressionRoot), "Template uses \"" + expressionRoot + "\", but no @ModelData with that name exists on " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private void validateConsumedDataIsProvided(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (String consumedDataName : usage.consumedDataNames()) {
            if (!controller.providesData(consumedDataName)) {
                errors.add(usage.consumedDataLine(consumedDataName), "Template binds \"" + consumedDataName + "\", but no @ModelData or @FormData with that name exists on " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private void validateFormFieldsAreProvided(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (var formEntry : usage.formFieldLines().entrySet()) {
            TemplateDataModel formData = controller.formData(formEntry.getKey());
            if (formData == null) {
                continue;
            }
            for (var fieldEntry : formEntry.getValue().entrySet()) {
                if (!formData.hasField(fieldEntry.getKey())) {
                    errors.add(fieldEntry.getValue(), "Template binds field \"" + fieldEntry.getKey() + "\" on @FormData \"" + formData.name() + "\", but that field does not exist on the form object.");
                    if (errors.shouldStop()) {
                        return;
                    }
                }
            }
        }
    }

    private void validatePropertyPathsAreProvided(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (PropertyPathUsage propertyPath : usage.propertyPaths()) {
            TemplateDataModel model = propertyPath.localVariable()
                    ? usage.localVariable(propertyPath.root())
                    : controller.modelData(propertyPath.root());
            if (model == null) {
                continue;
            }
            String invalidProperty = firstInvalidProperty(model, propertyPath.properties());
            if (invalidProperty != null) {
                errors.add(propertyPath.line(), "Template uses property \"" + invalidProperty + "\" in \"" + propertyPath.path() + "\", but that property does not exist on the bound object.");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private String firstInvalidProperty(TemplateDataModel model, List<String> properties) {
        TemplateDataModel current = model;
        for (String property : properties) {
            if (!current.hasField(property)) {
                return property;
            }
            current = current.field(property);
            if (current == null) {
                return null;
            }
        }
        return null;
    }

    private String readTemplate(Path templateFile) {
        try {
            return Files.readString(templateFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class LineNumberResolver {
        private final String content;
        private final Map<String, Integer> searchOffsets = new HashMap<>();

        LineNumberResolver(String content) {
            this.content = content;
        }

        int lineNumber(Element element) {
            int startIndex = startIndex(element);
            if (startIndex < 0) {
                return 1;
            }

            int line = 1;
            for (int index = 0; index < startIndex; index++) {
                if (content.charAt(index) == '\n') {
                    line++;
                }
            }
            return line;
        }

        private int startIndex(Element element) {
            String localName = element.getLocalName();
            Pattern openingTagPattern = Pattern.compile("<" + Pattern.quote(localName) + "(?=[\\s>/])");
            Matcher matcher = openingTagPattern.matcher(content);
            int searchOffset = searchOffsets.getOrDefault(localName, 0);
            if (!matcher.find(searchOffset)) {
                return -1;
            }
            searchOffsets.put(localName, matcher.end());
            return matcher.start();
        }
    }
}
