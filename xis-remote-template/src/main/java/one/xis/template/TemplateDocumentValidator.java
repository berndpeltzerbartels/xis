package one.xis.template;

import lombok.RequiredArgsConstructor;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static one.xis.template.TemplateDocumentParser.ATTR_IF;
import static one.xis.template.TemplateDocumentParser.ATTR_REPEAT;

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
        XmlUtil.getElementByTagName(root, "head").orElseThrow(() -> new TemplateSynthaxException("page must contain head-element"));
        XmlUtil.getElementByTagName(root, "body").orElseThrow(() -> new TemplateSynthaxException("page must contain body-element"));

        Element body = XmlUtil.getElementByTagName(root, "body").orElseThrow();
        if (body.hasAttribute(ATTR_REPEAT)) {
            throw new TemplateSynthaxException("body must not have attribute " + ATTR_REPEAT);
        }
        if (body.hasAttribute(ATTR_IF)) {
            throw new TemplateSynthaxException("body must not have attribute " + ATTR_IF);
        }
    }
}
