package one.xis.template;

import lombok.RequiredArgsConstructor;
import org.w3c.dom.Document;

import static one.xis.template.TemplateParser.ATTR_IF;
import static one.xis.template.TemplateParser.ATTR_REPEAT;

@RequiredArgsConstructor
public class TemplateDocumentValidator {
    private final Document document;

    public void validatePageTemplate() {
        var root = document.getDocumentElement();
        if (root.hasAttribute(ATTR_IF)) {
            throw new TemplateSynthaxException("top-level elements of a page must not have " + ATTR_IF);
        }
        if (root.hasAttribute(ATTR_REPEAT)) {
            throw new TemplateSynthaxException("top-level elements of a page must not have " + ATTR_REPEAT);
        }
    }
}
