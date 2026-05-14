package one.xis.processor;

import one.xis.html.document.Element;

import java.util.Set;

class TemplateValidationRules {

    private static final Set<String> LINK_TAGS = Set.of("a", "button");
    private static final Set<String> FORM_FIELD_TAGS = Set.of("input", "textarea", "select", "form", "label");
    private static final Set<String> STORE_NAMES = Set.of("localStorage", "sessionStorage", "clientStorage");

    void validateElement(TemplateElement element, ValidationErrorCollector errors) {
        validateRequiredAttributes(element, errors);
        validateRequiredAlternativeAttributes(element, errors);
        validateRequiredAttributeValues(element, errors);
        validateAttributeDependencies(element, errors);
        validateAttributePlacement(element, errors);
        validateKnownValueSets(element, errors);
    }

    private void validateRequiredAttributes(TemplateElement element, ValidationErrorCollector errors) {
        for (RequiredTagAttribute rule : RequiredTagAttribute.values()) {
            if (rule.matches(element.raw())) {
                errors.add(element.line(), rule.message());
            }
        }
    }

    private void validateAttributeDependencies(TemplateElement element, ValidationErrorCollector errors) {
        for (RequiredPeerAttribute rule : RequiredPeerAttribute.values()) {
            if (rule.matches(element.raw())) {
                errors.add(element.line(), rule.message());
            }
        }
        if (hasSelectionClassWithoutGroup(element.raw())) {
            errors.add(element.line(), "xis:selection-class requires a surrounding element with xis:selection-group or selection-group.");
        }
    }

    private void validateRequiredAlternativeAttributes(TemplateElement element, ValidationErrorCollector errors) {
        for (RequiredAlternativeTagAttribute rule : RequiredAlternativeTagAttribute.values()) {
            if (rule.matches(element.raw())) {
                errors.add(element.line(), rule.message());
            }
        }
    }

    private void validateAttributePlacement(TemplateElement element, ValidationErrorCollector errors) {
        for (AttributePlacement rule : AttributePlacement.values()) {
            if (rule.violatedBy(element.raw())) {
                errors.add(element.line(), rule.message());
            }
        }
    }

    private void validateRequiredAttributeValues(TemplateElement element, ValidationErrorCollector errors) {
        for (RequiredAttributeValue rule : RequiredAttributeValue.values()) {
            if (rule.violatedBy(element.raw())) {
                errors.add(element.line(), rule.message());
            }
        }
    }

    private void validateKnownValueSets(TemplateElement element, ValidationErrorCollector errors) {
        String storageBinding = element.raw().getAttributes().get("xis:storage-binding");
        if (storageBinding != null && !STORE_NAMES.contains(storageBinding)) {
            errors.add(element.line(), "xis:storage-binding must be one of localStorage, sessionStorage, or clientStorage.");
        }
        String storageTagStore = element.raw().getAttributes().get("store");
        if ("xis:storage-binding".equals(element.raw().getLocalName()) && storageTagStore != null && !STORE_NAMES.contains(storageTagStore)) {
            errors.add(element.line(), "<xis:storage-binding> store must be one of localStorage, sessionStorage, or clientStorage.");
        }
    }

    private boolean hasSelectionClassWithoutGroup(Element element) {
        if (!hasAttribute(element, "xis:selection-class") && !hasAttribute(element, "selection-class")) {
            return false;
        }
        Element parent = element.getParentNode();
        while (parent != null) {
            if (hasAttribute(parent, "xis:selection-group") || hasAttribute(parent, "selection-group")) {
                return false;
            }
            parent = parent.getParentNode();
        }
        return true;
    }

    private static boolean hasAttribute(Element element, String attributeName) {
        return element.getAttributes().containsKey(attributeName);
    }

    private enum RequiredPeerAttribute {
        FORMAT_BINDING("xis:format", Set.of("xis:binding"), "xis:format requires xis:binding on the same element."),
        ERROR_CLASS_BINDING("xis:error-class", Set.of("xis:binding", "xis:error-binding"), "xis:error-class requires xis:binding or xis:error-binding on the same element."),
        ERROR_STYLE_BINDING("xis:error-style", Set.of("xis:binding", "xis:error-binding"), "xis:error-style requires xis:binding or xis:error-binding on the same element."),
        DEFAULT_FRONTLET_CONTAINER("xis:default-frontlet", Set.of("xis:frontlet-container"), "xis:default-frontlet requires xis:frontlet-container on the same element."),
        SCROLL_TO_TOP_CONTAINER("xis:scroll-to-top", Set.of("xis:frontlet-container"), "xis:scroll-to-top requires xis:frontlet-container on the same element.");

        private final String attributeName;
        private final Set<String> requiredAlternatives;
        private final String message;

        RequiredPeerAttribute(String attributeName, Set<String> requiredAlternatives, String message) {
            this.attributeName = attributeName;
            this.requiredAlternatives = requiredAlternatives;
            this.message = message;
        }

        boolean matches(Element element) {
            return hasAttribute(element, attributeName) && requiredAlternatives.stream().noneMatch(attribute -> hasAttribute(element, attribute));
        }

        String message() {
            return message;
        }
    }

    private enum RequiredAttributeValue {
        IF_ATTRIBUTE("xis:if", "xis:if requires a condition expression."),
        FOREACH_ATTRIBUTE("xis:foreach", "xis:foreach must use var:array syntax."),
        REPEAT_ATTRIBUTE("xis:repeat", "xis:repeat must use var:array syntax."),
        DRAG_ATTRIBUTE("xis:drag", "xis:drag must use name:expression syntax."),
        DROP_ATTRIBUTE("xis:drop", "xis:drop must use actionName(arg1, arg2) syntax."),
        SELECTION_CLASS_ATTRIBUTE("xis:selection-class", "xis:selection-class requires a CSS class expression."),
        SELECTION_CLASS_ALIAS("selection-class", "selection-class requires a CSS class expression."),
        INCLUDE_ATTRIBUTE("xis:include", "xis:include requires an include name."),
        FRONTLET_CONTAINER_ATTRIBUTE("xis:frontlet-container", "xis:frontlet-container requires a container id."),
        MESSAGE_FOR_ATTRIBUTE("xis:message-for", "xis:message-for requires a binding name."),
        STORAGE_BINDING_ATTRIBUTE("xis:storage-binding", "xis:storage-binding requires a store name."),
        PAGE_ATTRIBUTE("xis:page", "xis:page requires a page URL or page id."),
        FRONTLET_ATTRIBUTE("xis:frontlet", "xis:frontlet requires a frontlet URL or frontlet id."),
        MODAL_ATTRIBUTE("xis:modal", "xis:modal requires a modal URL or modal id."),
        ACTION_ATTRIBUTE("xis:action", "xis:action requires an action name."),
        TARGET_CONTAINER_ATTRIBUTE("xis:target-container", "xis:target-container requires a container id."),
        DEFAULT_FRONTLET_ATTRIBUTE("xis:default-frontlet", "xis:default-frontlet requires a frontlet id."),
        PAGE_ELEMENT("page", "page requires a page URL or page id."),
        FRONTLET_ELEMENT("frontlet", "frontlet requires a frontlet URL or frontlet id."),
        ACTION_ELEMENT("action", "action requires an action name."),
        BINDING_ELEMENT("binding", "binding requires a binding expression."),
        DEFAULT_FRONTLET_ELEMENT("default-frontlet", "default-frontlet requires a frontlet id.");

        private final String attributeName;
        private final String message;

        RequiredAttributeValue(String attributeName, String message) {
            this.attributeName = attributeName;
            this.message = message;
        }

        boolean violatedBy(Element element) {
            String value = element.getAttributes().get(attributeName);
            if (value == null) {
                return false;
            }
            if (value.isBlank()) {
                return true;
            }
            if ("xis:foreach".equals(attributeName) || "xis:repeat".equals(attributeName) || "xis:drag".equals(attributeName)) {
                return !hasNameAndExpression(value);
            }
            if ("xis:drop".equals(attributeName)) {
                return !hasActionCall(value);
            }
            return false;
        }

        String message() {
            return message;
        }

        private boolean hasNameAndExpression(String value) {
            int separator = value.indexOf(':');
            return separator > 0 && separator < value.length() - 1;
        }

        private boolean hasActionCall(String value) {
            int open = value.indexOf('(');
            int close = value.lastIndexOf(')');
            return open > 0 && close > open;
        }
    }

    private enum RequiredTagAttribute {
        IF_CONDITION("xis:if", Set.of("condition"), "xis:if requires condition."),
        FOREACH_VAR("xis:foreach", Set.of("var"), "xis:foreach requires var."),
        FOREACH_ARRAY("xis:foreach", Set.of("array"), "xis:foreach requires array."),
        MESSAGE_FOR("xis:message", Set.of("message-for"), "xis:message requires message-for."),
        INCLUDE_NAME("xis:include", Set.of("name"), "xis:include requires name."),
        FRONTLET_CONTAINER_ID("xis:frontlet-container", Set.of("container-id"), "xis:frontlet-container requires container-id."),
        PARAMETER_NAME("xis:parameter", Set.of("name"), "xis:parameter requires name."),
        STORAGE_BINDING_STORE("xis:storage-binding", Set.of("store"), "xis:storage-binding requires store."),
        FRAMEWORK_FORM_BINDING("xis:form", Set.of("binding"), "xis:form requires binding."),
        FRAMEWORK_INPUT_BINDING("xis:input", Set.of("binding"), "xis:input requires binding."),
        FRAMEWORK_TEXTAREA_BINDING("xis:textarea", Set.of("binding"), "xis:textarea requires binding."),
        FRAMEWORK_SELECT_BINDING("xis:select", Set.of("binding"), "xis:select requires binding."),
        FRAMEWORK_CHECKBOX_BINDING("xis:checkbox", Set.of("binding"), "xis:checkbox requires binding."),
        FRAMEWORK_RADIO_BINDING("xis:radio", Set.of("binding"), "xis:radio requires binding."),
        FRAMEWORK_SUBMIT_ACTION("xis:submit", Set.of("action"), "xis:submit requires action."),
        SHORT_FRONTLET_NAME("xis:frontlet", Set.of("name"), "xis:frontlet requires name.");

        private final String tagName;
        private final Set<String> requiredAttributes;
        private final String message;

        RequiredTagAttribute(String tagName, Set<String> requiredAttributes, String message) {
            this.tagName = tagName;
            this.requiredAttributes = requiredAttributes;
            this.message = message;
        }

        boolean matches(Element element) {
            return tagName.equals(element.getLocalName()) && requiredAttributes.stream().noneMatch(attribute -> hasAttribute(element, attribute));
        }

        String message() {
            return message;
        }
    }

    private enum RequiredAlternativeTagAttribute {
        FRAMEWORK_LINK("xis:a", Set.of("page", "frontlet", "modal"), "xis:a requires page, frontlet, or modal."),
        FRAMEWORK_BUTTON("xis:button", Set.of("page", "frontlet", "modal", "action"), "xis:button requires page, frontlet, modal, or action."),
        FRAMEWORK_ACTION("xis:action", Set.of("action"), "xis:action requires action.");

        private final String tagName;
        private final Set<String> alternatives;
        private final String message;

        RequiredAlternativeTagAttribute(String tagName, Set<String> alternatives, String message) {
            this.tagName = tagName;
            this.alternatives = alternatives;
            this.message = message;
        }

        boolean matches(Element element) {
            return tagName.equals(element.getLocalName()) && alternatives.stream().noneMatch(attribute -> hasAttribute(element, attribute));
        }

        String message() {
            return message;
        }
    }

    private enum AttributePlacement {
        LINK_TARGETS("xis:page, xis:frontlet, and xis:modal are only supported on <a> and <button>.", Set.of("xis:page", "xis:frontlet", "xis:modal"), LINK_TAGS),
        ACTION_TARGET("xis:action is only supported on <a>, <button>, and <submit>.", Set.of("xis:action"), Set.of("a", "button", "submit")),
        SUBMIT_ON_KEYUP("xis:submit-onkeyup is only supported on form fields.", Set.of("xis:submit-onkeyup"), FORM_FIELD_TAGS),
        FORM_BINDING("xis:binding on form fields is only supported on <form>, <input>, <textarea>, <select>, and <label>.", Set.of("xis:binding"), FORM_FIELD_TAGS);

        private final String message;
        private final Set<String> attributes;
        private final Set<String> allowedTags;

        AttributePlacement(String message, Set<String> attributes, Set<String> allowedTags) {
            this.message = message;
            this.attributes = attributes;
            this.allowedTags = allowedTags;
        }

        boolean violatedBy(Element element) {
            return attributes.stream().anyMatch(attribute -> hasAttribute(element, attribute))
                    && !allowedTags.contains(element.getLocalName());
        }

        String message() {
            return message;
        }
    }
}
