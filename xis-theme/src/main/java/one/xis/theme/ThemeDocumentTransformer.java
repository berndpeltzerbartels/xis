package one.xis.theme;

import one.xis.context.Component;
import one.xis.html.document.Element;
import one.xis.html.document.HtmlDocument;
import one.xis.html.document.Node;
import one.xis.html.document.TextNode;
import one.xis.server.HtmlDocumentTransformer;

import java.util.Map;
import java.util.Set;

@Component
public class ThemeDocumentTransformer implements HtmlDocumentTransformer {

    private static final String THEME_WRAPPER = "xt:wrapper";
    private static final String THEME_GRID = "xt:grid";
    private static final String THEME_SPAN = "xt:span";
    private static final String THEME_FIELD = "xt:field";
    private static final Set<String> THEME_FORM_ATTRIBUTES = Set.of("binding", "action", "submit-label");
    private static final Set<String> THEME_FORM_PAGE_ATTRIBUTES = Set.of("title", "binding", "action", "submit-label");
    private static final Set<String> THEME_INPUT_ATTRIBUTES = Set.of("binding", "title", "label", "span");
    private static final Set<String> THEME_TEXTAREA_ATTRIBUTES = Set.of("binding", "title", "label", "span");
    private static final Set<String> THEME_CHECKBOX_ATTRIBUTES = Set.of("binding", "title", "label", "span");
    private static final Set<String> THEME_RADIO_ATTRIBUTES = Set.of("binding", "title", "label", "span", "options", "option-var", "option-value", "option-label");
    private static final Set<String> THEME_SELECT_ATTRIBUTES = Set.of("binding", "title", "label", "span", "options", "option-var", "option-value", "option-label");
    private static final Set<String> THEME_GRID_ATTRIBUTES = Set.of("as", "columns", "rows");
    private static final Set<String> THEME_NAVIGATION_ATTRIBUTES = Set.of("logo", "logo-alt");
    private static final Set<String> THEME_NAV_ITEM_ATTRIBUTES = Set.of("page", "frontlet", "modal", "href", "label");
    private static final Set<String> THEME_NAV_GROUP_ATTRIBUTES = Set.of("label");

    @Override
    public HtmlDocument transform(HtmlDocument document) {
        transform(document.getDocumentElement());
        return document;
    }

    private void transform(Element element) {
        element = replaceThemeTag(element);
        applyThemeAttributes(element);
        var child = element.getFirstChild();
        while (child != null) {
            var next = child.getNextSibling();
            if (child instanceof Element childElement) {
                transform(childElement);
            }
            child = next;
        }
    }

    private Element replaceThemeTag(Element element) {
        Element replacement = switch (element.getLocalName()) {
            case "xt:form" -> form(element);
            case "xt:form-page" -> formPage(element);
            case "xt:input" -> inputField(element);
            case "xt:textarea" -> textareaField(element);
            case "xt:checkbox" -> checkboxField(element);
            case "xt:radio" -> radioField(element);
            case "xt:select" -> selectField(element);
            case "xt:grid" -> grid(element);
            case "xt:navigation" -> navigation(element);
            case "xt:nav-item" -> navItem(element);
            case "xt:nav-group" -> navGroup(element);
            default -> null;
        };
        if (replacement == null) {
            return element;
        }
        replaceElement(element, replacement);
        return replacement;
    }

    private Element form(Element source) {
        var binding = requiredAttribute(source, "binding");
        var form = new Element("form");
        copyAttributes(source, form, "xt:form-page".equals(source.getLocalName()) ? THEME_FORM_PAGE_ATTRIBUTES : THEME_FORM_ATTRIBUTES);
        form.setAttribute("xis:binding", binding);
        moveChildren(source, form);
        var action = source.getAttributes().get("action");
        if (action != null && !action.isBlank()) {
            form.appendChild(submitButton(action, source.getAttributes().getOrDefault("submit-label", humanize(action))));
        }
        return form;
    }

    private Element formPage(Element source) {
        var page = new Element("main");
        copyAttributes(source, page, THEME_FORM_PAGE_ATTRIBUTES);
        appendClass(page, "wrapper");
        var title = source.getAttributes().get("title");
        if (title != null && !title.isBlank()) {
            var heading = new Element("h1");
            heading.appendChild(new TextNode(title));
            page.appendChild(heading);
        }
        page.appendChild(form(source));
        return page;
    }

    private Element inputField(Element source) {
        var binding = requiredAttribute(source, "binding");
        var id = source.getAttributes().getOrDefault("id", fieldId(binding));
        var wrapper = fieldWrapper(source);
        wrapper.appendChild(label(id, binding, titleText(source)));
        wrapper.appendChild(input(source, binding, id));
        wrapper.appendChild(message(binding));
        return wrapper;
    }

    private Element textareaField(Element source) {
        var binding = requiredAttribute(source, "binding");
        var id = source.getAttributes().getOrDefault("id", fieldId(binding));
        var wrapper = fieldWrapper(source);
        wrapper.appendChild(label(id, binding, titleText(source)));
        wrapper.appendChild(textarea(source, binding, id));
        wrapper.appendChild(message(binding));
        return wrapper;
    }

    private Element checkboxField(Element source) {
        var binding = requiredAttribute(source, "binding");
        var id = source.getAttributes().getOrDefault("id", fieldId(binding));
        var wrapper = fieldWrapper(source);
        var label = checkboxLabel(id, binding, titleText(source));
        label.appendChild(checkbox(source, binding, id));
        label.appendChild(new TextNode(titleText(source)));
        wrapper.appendChild(label);
        wrapper.appendChild(message(binding));
        return wrapper;
    }

    private Element grid(Element source) {
        var columns = requiredAttribute(source, "columns");
        var grid = new Element(source.getAttributes().getOrDefault("as", "section"));
        copyAttributes(source, grid, THEME_GRID_ATTRIBUTES);
        appendClass(grid, "col" + checkedNumber("columns", columns, 2, 11));
        moveChildren(source, grid);
        return grid;
    }

    private Element navigation(Element source) {
        var nav = new Element("nav");
        copyAttributes(source, nav, THEME_NAVIGATION_ATTRIBUTES);
        appendClass(nav, "nav");
        nav.appendChild(logo(source));
        var list = new Element("ul");
        moveChildren(source, list);
        nav.appendChild(list);
        return nav;
    }

    private Element logo(Element source) {
        var logo = new Element("div");
        logo.setAttribute("class", "logo");
        var img = new Element("img");
        img.setAttribute("src", source.getAttributes().getOrDefault("logo", "/default-theme-logo.svg"));
        img.setAttribute("alt", source.getAttributes().getOrDefault("logo-alt", "Logo"));
        logo.appendChild(img);
        return logo;
    }

    private Element navItem(Element source) {
        var item = new Element("li");
        item.appendChild(navLink(source));
        return item;
    }

    private Element navGroup(Element source) {
        var item = new Element("li");
        item.appendChild(groupLink(requiredAttribute(source, "label")));
        var list = new Element("ul");
        moveChildren(source, list);
        item.appendChild(list);
        return item;
    }

    private Element navLink(Element source) {
        var link = new Element("a");
        copyAttributes(source, link, THEME_NAV_ITEM_ATTRIBUTES);
        var page = source.getAttributes().get("page");
        var frontlet = source.getAttributes().get("frontlet");
        var modal = source.getAttributes().get("modal");
        var href = source.getAttributes().get("href");
        if (page != null && !page.isBlank()) {
            link.setAttribute("xis:page", page);
        } else if (frontlet != null && !frontlet.isBlank()) {
            link.setAttribute("xis:frontlet", frontlet);
        } else if (modal != null && !modal.isBlank()) {
            link.setAttribute("xis:modal", modal);
        } else {
            link.setAttribute("href", href == null || href.isBlank() ? "#" : href);
        }
        link.appendChild(new TextNode(source.getAttributes().getOrDefault("label", navLabel(source))));
        return link;
    }

    private Element groupLink(String label) {
        var link = new Element("a");
        link.setAttribute("href", "#");
        link.appendChild(new TextNode(label));
        return link;
    }

    private String navLabel(Element source) {
        var value = source.getAttributes().get("page");
        if (value == null || value.isBlank()) {
            value = source.getAttributes().get("frontlet");
        }
        if (value == null || value.isBlank()) {
            value = source.getAttributes().get("modal");
        }
        if (value == null || value.isBlank()) {
            value = source.getAttributes().get("href");
        }
        if (value == null || value.isBlank()) {
            return "Link";
        }
        var slash = value.lastIndexOf('/');
        if (slash >= 0 && slash < value.length() - 1) {
            value = value.substring(slash + 1);
        }
        if (value.endsWith(".html")) {
            value = value.substring(0, value.length() - ".html".length());
        }
        return humanize(value);
    }

    private Element selectField(Element source) {
        var binding = requiredAttribute(source, "binding");
        var options = requiredAttribute(source, "options");
        var id = source.getAttributes().getOrDefault("id", fieldId(binding));
        var wrapper = fieldWrapper(source);
        wrapper.appendChild(label(id, binding, titleText(source)));
        wrapper.appendChild(select(source, binding, id, options));
        wrapper.appendChild(message(binding));
        return wrapper;
    }

    private Element radioField(Element source) {
        var binding = requiredAttribute(source, "binding");
        var options = requiredAttribute(source, "options");
        var wrapper = fieldWrapper(source);
        appendClass(wrapper, "radio-group");
        wrapper.appendChild(groupTitle(binding, titleText(source)));
        var choices = new Element("div");
        appendClass(choices, "choice-row");
        choices.appendChild(radioOption(source, binding, options));
        wrapper.appendChild(choices);
        wrapper.appendChild(message(binding));
        return wrapper;
    }

    private Element fieldWrapper(Element source) {
        var wrapper = new Element("div");
        wrapper.setAttribute("class", "form-field");
        var span = source.getAttributes().get("span");
        if (span != null && !span.isBlank()) {
            appendClass(wrapper, "span" + checkedNumber("span", span, 1, 9));
        }
        return wrapper;
    }

    private Element label(String id, String binding, String text) {
        var label = new Element("label");
        label.setAttribute("for", id);
        label.setAttribute("xis:binding", binding);
        label.setAttribute("xis:error-class", "error");
        label.appendChild(new TextNode(text));
        return label;
    }

    private Element checkboxLabel(String id, String binding, String text) {
        var label = new Element("label");
        label.setAttribute("for", id);
        label.setAttribute("xis:binding", binding);
        label.setAttribute("xis:error-class", "error");
        label.setAttribute("aria-label", text);
        return label;
    }

    private Element input(Element source, String binding, String id) {
        var input = new Element("input");
        copyAttributes(source, input, THEME_INPUT_ATTRIBUTES);
        input.setAttribute("id", id);
        input.setAttribute("type", source.getAttributes().getOrDefault("type", "text"));
        input.setAttribute("xis:binding", binding);
        input.setAttribute("xis:error-class", "error");
        return input;
    }

    private Element textarea(Element source, String binding, String id) {
        var textarea = new Element("textarea");
        copyAttributes(source, textarea, THEME_TEXTAREA_ATTRIBUTES);
        textarea.setAttribute("id", id);
        textarea.setAttribute("xis:binding", binding);
        textarea.setAttribute("xis:error-class", "error");
        return textarea;
    }

    private Element checkbox(Element source, String binding, String id) {
        var checkbox = new Element("input");
        copyAttributes(source, checkbox, THEME_CHECKBOX_ATTRIBUTES);
        checkbox.setAttribute("id", id);
        checkbox.setAttribute("type", "checkbox");
        checkbox.setAttribute("xis:binding", binding);
        checkbox.setAttribute("xis:error-class", "error");
        return checkbox;
    }

    private Element select(Element source, String binding, String id, String options) {
        var select = new Element("select");
        copyAttributes(source, select, THEME_SELECT_ATTRIBUTES);
        select.setAttribute("id", id);
        select.setAttribute("xis:binding", binding);
        select.setAttribute("xis:error-class", "error");
        select.appendChild(option(source, options));
        return select;
    }

    private Element option(Element source, String options) {
        var variable = source.getAttributes().getOrDefault("option-var", "option");
        var value = source.getAttributes().getOrDefault("option-value", variable);
        var label = source.getAttributes().getOrDefault("option-label", variable);
        var option = new Element("option");
        option.setAttribute("xis:repeat", variable + ":" + options);
        option.setAttribute("value", optionExpression(variable, value));
        option.appendChild(new TextNode(optionExpression(variable, label)));
        return option;
    }

    private Element radioOption(Element source, String binding, String options) {
        var variable = source.getAttributes().getOrDefault("option-var", "option");
        var value = source.getAttributes().getOrDefault("option-value", variable);
        var label = source.getAttributes().getOrDefault("option-label", variable);
        var optionLabel = new Element("label");
        optionLabel.setAttribute("xis:repeat", variable + ":" + options);
        var input = new Element("input");
        copyAttributes(source, input, THEME_RADIO_ATTRIBUTES);
        input.getAttributes().remove("id");
        input.setAttribute("type", "radio");
        input.setAttribute("xis:binding", binding);
        input.setAttribute("value", optionExpression(variable, value));
        optionLabel.appendChild(input);
        var text = new Element("span");
        text.appendChild(new TextNode(optionExpression(variable, label)));
        optionLabel.appendChild(text);
        return optionLabel;
    }

    private Element groupTitle(String binding, String text) {
        var title = new Element("div");
        title.setAttribute("xis:binding", binding);
        title.setAttribute("xis:error-class", "error");
        title.appendChild(new TextNode(text));
        return title;
    }

    private Element message(String binding) {
        var message = new Element("div");
        message.setAttribute("xis:message-for", binding);
        return message;
    }

    private Element submitButton(String action, String text) {
        var button = new Element("button");
        button.setAttribute("type", "submit");
        button.setAttribute("xis:action", action);
        button.appendChild(new TextNode(text));
        return button;
    }

    private String titleText(Element source) {
        return requiredAttribute(source, "title");
    }

    private String requiredAttribute(Element source, String name) {
        var value = source.getAttributes().get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("<" + source.getLocalName() + "> requires " + name + ".");
        }
        return value;
    }

    private String fieldId(String binding) {
        return binding.replaceAll("[^A-Za-z0-9_-]+", "-");
    }

    private String humanize(String value) {
        var simple = value;
        var dot = simple.lastIndexOf('.');
        if (dot >= 0 && dot < simple.length() - 1) {
            simple = simple.substring(dot + 1);
        }
        var text = simple.replace('-', ' ').replace('_', ' ');
        text = text.replaceAll("([a-z])([A-Z])", "$1 $2");
        if (text.isBlank()) {
            return value;
        }
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private String expression(String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            return value;
        }
        return "${" + value + "}";
    }

    private String optionExpression(String variable, String value) {
        if (value.startsWith("${") && value.endsWith("}")) {
            return value;
        }
        if (value.equals(variable) || value.contains(".")) {
            return expression(value);
        }
        return expression(variable + "." + value);
    }

    private void moveChildren(Element source, Element target) {
        var child = source.getFirstChild();
        source.setFirstChild(null);
        target.setFirstChild(child);
        while (child != null) {
            child.setParentNode(target);
            child = child.getNextSibling();
        }
    }

    private void copyAttributes(Element source, Element target, Set<String> themeAttributes) {
        for (Map.Entry<String, String> entry : source.getAttributes().entrySet()) {
            var name = entry.getKey();
            if (themeAttributes.contains(name) || name.startsWith("xt:")) {
                continue;
            }
            target.setAttribute(name, entry.getValue());
        }
    }

    private void replaceElement(Element source, Element target) {
        var parent = source.getParentNode();
        target.setParentNode(parent);
        target.setNextSibling(source.getNextSibling());
        source.setNextSibling(null);
        if (parent == null) {
            return;
        }
        if (parent.getFirstChild() == source) {
            parent.setFirstChild(target);
            return;
        }
        Node current = parent.getFirstChild();
        while (current != null) {
            if (current.getNextSibling() == source) {
                current.setNextSibling(target);
                return;
            }
            current = current.getNextSibling();
        }
    }

    private void applyThemeAttributes(Element element) {
        if (removeBooleanAttribute(element, THEME_WRAPPER)) {
            appendClass(element, "wrapper");
        }
        if (removeBooleanAttribute(element, THEME_FIELD)) {
            appendClass(element, "form-field");
        }
        var grid = element.getAttributes().remove(THEME_GRID);
        if (grid != null) {
            appendClass(element, "col" + checkedNumber(THEME_GRID, grid, 2, 11));
        }
        var span = element.getAttributes().remove(THEME_SPAN);
        if (span != null) {
            appendClass(element, "span" + checkedNumber(THEME_SPAN, span, 1, 9));
        }
    }

    private boolean removeBooleanAttribute(Element element, String attribute) {
        return element.getAttributes().remove(attribute) != null;
    }

    private int checkedNumber(String attribute, String value, int min, int max) {
        try {
            var number = Integer.parseInt(value);
            if (number >= min && number <= max) {
                return number;
            }
        } catch (NumberFormatException ignored) {
            // handled below
        }
        throw new IllegalArgumentException(attribute + " must be a number between " + min + " and " + max + ": " + value);
    }

    private void appendClass(Element element, String className) {
        var currentClass = element.getAttributes().get("class");
        if (currentClass == null || currentClass.isBlank()) {
            element.setAttribute("class", className);
            return;
        }
        element.setAttribute("class", currentClass + " " + className);
    }
}
