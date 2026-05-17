package one.xis.html.validation;

import one.xis.html.document.Element;

import java.util.List;
import java.util.Optional;

public interface TemplateValidationExtension {

    default Optional<String> formDataBinding(Element element) {
        return Optional.empty();
    }

    default Optional<String> formFieldBinding(Element element) {
        return Optional.empty();
    }

    default List<String> modelDataBindings(Element element) {
        return List.of();
    }

    default List<String> validate(Element element) {
        return List.of();
    }
}
