package one.xis.theme;

import one.xis.html.document.Element;
import one.xis.html.validation.TemplateValidationExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ThemeTemplateValidationExtension implements TemplateValidationExtension {

    @Override
    public Optional<String> formDataBinding(Element element) {
        if ("xt:form".equals(element.getLocalName()) || "xt:form-page".equals(element.getLocalName())) {
            return Optional.ofNullable(element.getAttributes().get("binding"));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> formFieldBinding(Element element) {
        return switch (element.getLocalName()) {
            case "xt:input", "xt:select", "xt:textarea", "xt:checkbox", "xt:radio" ->
                    Optional.ofNullable(element.getAttributes().get("binding"));
            default -> Optional.empty();
        };
    }

    @Override
    public List<String> modelDataBindings(Element element) {
        if ("xt:select".equals(element.getLocalName()) || "xt:radio".equals(element.getLocalName())) {
            var options = element.getAttributes().get("options");
            if (options != null && !options.isBlank()) {
                return List.of(options);
            }
        }
        return List.of();
    }

    @Override
    public List<String> validate(Element element) {
        List<String> messages = new ArrayList<>();
        switch (element.getLocalName()) {
            case "xt:form-page" -> require(element, messages, "binding", "xt:form-page requires binding.");
            case "xt:form" -> require(element, messages, "binding", "xt:form requires binding.");
            case "xt:input" -> validateField(element, messages, "xt:input");
            case "xt:textarea" -> validateField(element, messages, "xt:textarea");
            case "xt:checkbox" -> validateField(element, messages, "xt:checkbox");
            case "xt:select" -> validateOptionField(element, messages, "xt:select");
            case "xt:radio" -> validateOptionField(element, messages, "xt:radio");
            case "xt:grid" -> requireNumber(element, messages, "columns", 2, 11, "xt:grid columns must be a number between 2 and 11.");
            case "xt:nav-group" -> require(element, messages, "label", "xt:nav-group requires label.");
            case "xt:nav-item" -> requireOneOf(element, messages, List.of("page", "frontlet", "modal", "href"), "xt:nav-item requires page, frontlet, modal, or href.");
            default -> {
            }
        }
        return messages;
    }

    private void validateField(Element element, List<String> messages, String tagName) {
        require(element, messages, "binding", tagName + " requires binding.");
        require(element, messages, "title", tagName + " requires title.");
        requireNumberIfPresent(element, messages, "span", 1, 9, tagName + " span must be a number between 1 and 9.");
    }

    private void validateOptionField(Element element, List<String> messages, String tagName) {
        validateField(element, messages, tagName);
        require(element, messages, "options", tagName + " requires options.");
    }

    private void require(Element element, List<String> messages, String attribute, String message) {
        var value = element.getAttributes().get(attribute);
        if (value == null || value.isBlank()) {
            messages.add(message);
        }
    }

    private void requireOneOf(Element element, List<String> messages, List<String> attributes, String message) {
        if (attributes.stream().noneMatch(attribute -> {
            var value = element.getAttributes().get(attribute);
            return value != null && !value.isBlank();
        })) {
            messages.add(message);
        }
    }

    private void requireNumber(Element element, List<String> messages, String attribute, int min, int max, String message) {
        var value = element.getAttributes().get(attribute);
        if (value == null || value.isBlank()) {
            messages.add("xt:grid requires columns.");
            return;
        }
        try {
            var number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return;
            }
        } catch (NumberFormatException ignored) {
            // handled below
        }
        messages.add(message);
    }

    private void requireNumberIfPresent(Element element, List<String> messages, String attribute, int min, int max, String message) {
        var value = element.getAttributes().get(attribute);
        if (value == null || value.isBlank()) {
            return;
        }
        try {
            var number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return;
            }
        } catch (NumberFormatException ignored) {
            // handled below
        }
        messages.add(message);
    }
}
