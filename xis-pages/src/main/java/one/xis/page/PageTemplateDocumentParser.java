package one.xis.page;

import lombok.NonNull;
import one.xis.context.XISComponent;
import one.xis.template.*;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import static one.xis.utils.xml.XmlUtil.getAttributes;

@XISComponent
class PageTemplateDocumentParser extends TemplateDocumentParser<PageTemplateModel> {

    PageTemplateDocumentParser(ExpressionParser expressionParser) {
        super(expressionParser);
    }

    @Override
    public PageTemplateModel parseTemplate(@NonNull Document document, @NonNull String controllerClass) {
        try {
            var pageModel = new PageTemplateModel(controllerClass);
            var root = document.getDocumentElement();
            var headElement = XmlUtil.getElementByTagName(root, "head").orElseThrow(() -> new TemplateSynthaxException(controllerClass + " must have head-tag")); // TODO create if not present
            var bodyElement = XmlUtil.getElementByTagName(root, "body").orElseThrow(() -> new TemplateSynthaxException(controllerClass + " must have body-tag"));  // TODO create if not present
            var headTemplateElement = toTemplateHeadElement(headElement);
            var bodyTemplateElement = toTemplateBodyElement(bodyElement);
            parseChildren(headElement).forEach(headTemplateElement::addChild);
            parseChildren(bodyElement).forEach(bodyTemplateElement::addChild);
            pageModel.setHead(headTemplateElement);
            pageModel.setBody(bodyTemplateElement);
            return pageModel;
        } catch (TemplateSynthaxException e) {
            throw new TemplateSynthaxException(String.format("Parsing failed for page '%s': %s", controllerClass, e.getMessage()));
        }
    }

    private TemplateHeadElement toTemplateHeadElement(Element element) {
        var templateElement = new TemplateHeadElement();
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, templateElement));
        return templateElement;
    }

    private TemplateBodyElement toTemplateBodyElement(Element element) {
        var templateElement = new TemplateBodyElement();
        getAttributes(element).forEach((name, rawValue) -> addAttribute(name, rawValue, templateElement));
        return templateElement;
    }
}
