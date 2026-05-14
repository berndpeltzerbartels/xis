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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class XisTemplateValidator {

    private static final Pattern TEXT_EXPRESSION_PATTERN = Pattern.compile("\\$\\{([^}]*)}");
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$]*");
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

        TemplateUsage usage = validateTemplate(projectDir, controller.templateFile(), errors);
        if (failFast && !errors.isEmpty()) {
            return;
        }
        validateControllerDataUsage(projectDir, controller, usage, errors);
    }

    private TemplateUsage validateTemplate(Path projectDir, Path templateFile, List<ValidationError> errors) {
        String content = readTemplate(templateFile);
        Element rootElement = parseTemplate(projectDir, templateFile, content, errors);
        TemplateUsage usage = new TemplateUsage();
        if (rootElement == null) {
            return usage;
        }

        ValidationErrorCollector errorCollector = new ValidationErrorCollector(failFast, projectDir, templateFile, errors);
        validateElement(content, rootElement, usage, errorCollector, new LinkedHashSet<>());
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
                                 TemplateUsage usage,
                                 ValidationErrorCollector errors,
                                 Set<String> localVariables) {
        int line = lineNumber(content, element);
        TemplateElement templateElement = new TemplateElement(element, line);
        rules.validateElement(templateElement, errors);
        collectElementUsage(element, usage, localVariables);
        if (errors.shouldStop()) {
            return;
        }

        Set<String> childLocalVariables = childLocalVariables(element, localVariables);
        validateChildren(content, element, usage, errors, childLocalVariables);
    }

    private void validateChildren(String content,
                                  Element element,
                                  TemplateUsage usage,
                                  ValidationErrorCollector errors,
                                  Set<String> localVariables) {
        Node child = element.getFirstChild();
        while (child != null) {
            validateChild(content, child, usage, errors, localVariables);
            if (errors.shouldStop()) {
                return;
            }
            child = child.getNextSibling();
        }
    }

    private void validateChild(String content,
                               Node child,
                               TemplateUsage usage,
                               ValidationErrorCollector errors,
                               Set<String> localVariables) {
        if (child instanceof Element childElement) {
            validateElement(content, childElement, usage, errors, localVariables);
        } else if (child instanceof TextNode textNode) {
            collectTextExpressions(textNode.getText(), usage, localVariables);
        }
    }

    private void collectElementUsage(Element element, TemplateUsage usage, Set<String> localVariables) {
        collectBindingUsage(element, usage);
        collectAttributeExpressions(element, usage, localVariables);
    }

    private void collectBindingUsage(Element element, TemplateUsage usage) {
        String binding = element.getAttributes().get("xis:binding");
        if (binding != null && "form".equals(element.getLocalName())) {
            usage.consumeData(firstPathSegment(binding));
        }
    }

    private void collectAttributeExpressions(Element element, TemplateUsage usage, Set<String> localVariables) {
        for (var entry : element.getAttributes().entrySet()) {
            String attributeName = entry.getKey();
            String attributeValue = entry.getValue();
            if ("xis:foreach".equals(attributeName) || "xis:repeat".equals(attributeName)) {
                collectForeachExpression(attributeValue, usage, localVariables);
            } else if ("array".equals(attributeName) && "xis:foreach".equals(element.getLocalName())) {
                collectExpression(attributeValue, usage, localVariables);
            } else if (attributeValue != null && attributeValue.contains("${")) {
                collectTextExpressions(attributeValue, usage, localVariables);
            } else if ("xis:if".equals(attributeName) || ("condition".equals(attributeName) && "xis:if".equals(element.getLocalName()))) {
                collectExpression(attributeValue, usage, localVariables);
            } else if ("xis:drag".equals(attributeName)) {
                collectDragExpression(attributeValue, usage, localVariables);
            } else if ("xis:drop".equals(attributeName)) {
                collectDropExpression(attributeValue, usage, localVariables);
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

    private String foreachVariable(Element element) {
        if ("xis:foreach".equals(element.getLocalName())) {
            return element.getAttributes().get("var");
        }
        String foreach = element.getAttributes().get("xis:foreach");
        if (foreach != null) {
            int separator = foreach.indexOf(':');
            if (separator > 0) {
                return foreach.substring(0, separator).trim();
            }
        }
        return null;
    }

    private void collectTextExpressions(String text, TemplateUsage usage, Set<String> localVariables) {
        Matcher matcher = TEXT_EXPRESSION_PATTERN.matcher(text);
        while (matcher.find()) {
            collectExpression(matcher.group(1), usage, localVariables);
        }
    }

    private void collectForeachExpression(String expression, TemplateUsage usage, Set<String> localVariables) {
        int separator = expression.indexOf(':');
        if (separator >= 0 && separator < expression.length() - 1) {
            collectExpression(expression.substring(separator + 1).trim(), usage, localVariables);
        }
    }

    private void collectDragExpression(String expression, TemplateUsage usage, Set<String> localVariables) {
        int separator = expression.indexOf(':');
        if (separator >= 0 && separator < expression.length() - 1) {
            collectExpression(expression.substring(separator + 1).trim(), usage, localVariables);
        }
    }

    private void collectDropExpression(String expression, TemplateUsage usage, Set<String> localVariables) {
        int open = expression.indexOf('(');
        int close = expression.lastIndexOf(')');
        if (open > 0 && close > open) {
            for (String argument : splitArguments(expression.substring(open + 1, close))) {
                collectExpression(dropArgumentExpression(argument), usage, localVariables);
            }
        }
    }

    private void collectExpression(String expression, TemplateUsage usage, Set<String> localVariables) {
        String normalized = removeStringLiterals(unwrapExpression(expression));
        Matcher matcher = IDENTIFIER_PATTERN.matcher(normalized);
        while (matcher.find()) {
            String identifier = matcher.group();
            if (isPropertyName(normalized, matcher.start())
                    || isFunctionCall(normalized, matcher.end())
                    || isIgnoredIdentifier(identifier, localVariables)) {
                continue;
            }
            usage.consumeExpressionRoot(identifier);
        }
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
            if (!controller.providesData(expressionRoot)) {
                errors.add(1, "Template uses \"" + expressionRoot + "\", but no @ModelData or @FormData with that name exists on " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private void validateConsumedDataIsProvided(ControllerTemplateModel controller, TemplateUsage usage, ValidationErrorCollector errors) {
        for (String consumedDataName : usage.consumedDataNames()) {
            if (!controller.providesData(consumedDataName)) {
                errors.add(1, "Template binds \"" + consumedDataName + "\", but no @ModelData or @FormData with that name exists on " + controller.controllerName() + ".");
                if (errors.shouldStop()) {
                    return;
                }
            }
        }
    }

    private String readTemplate(Path templateFile) {
        try {
            return Files.readString(templateFile, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private int lineNumber(String content, Element element) {
        String openingTagStart = "<" + element.getLocalName();
        int startIndex = content.indexOf(openingTagStart);
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
}
