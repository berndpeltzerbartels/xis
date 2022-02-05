package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Contains static strings and expressions.
 */
@Data
public class TextContent implements Container {
    private final List<TextElement> textElements = new ArrayList<>();

    @Override
    public String toString() {
        return textElements.stream().map(TextElement::toString).collect(Collectors.joining(""));
    }

    @Override
    public List<? extends TemplateElement> getElements() {
        return textElements;
    }

    @Override
    public void addElement(TemplateElement e) {
        if (e instanceof TextElement) {
            textElements.add((TextElement) e);
        }
        throw new IllegalArgumentException("TextContent must contain text-elements, only and not " + e);
    }

    public void addAllElements(Collection<TextElement> textElements) {
        this.textElements.addAll(textElements);
    }
}
