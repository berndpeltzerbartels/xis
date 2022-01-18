package one.xis.template;

import lombok.experimental.UtilityClass;

import java.util.stream.Stream;

@UtilityClass
class TemplateTestUtil {
    
    TemplateModel.IfElement assertRootIsIf(TemplateModel model, String condition) {
        if (model.getRoot() instanceof TemplateModel.IfElement) {
            TemplateModel.IfElement ifElement = (TemplateModel.IfElement) model.getRoot();
            if (ifElement.getCondition().equals(condition)) {
                return ifElement;
            }
        }
        throw new TemplateElementNotFoundException("if (" + condition + ")");
    }

    TemplateModel.ForElement assertRootIsFor(TemplateModel model, String arrayVar, String elementVar) {
        if (model.getRoot() instanceof TemplateModel.ForElement) {
            TemplateModel.ForElement forElement = (TemplateModel.ForElement) model.getRoot();
            if (forElement.getElementVarName().equals(elementVar) && forElement.getArrayVarName().equals(arrayVar)) {
                return forElement;
            }
        }
        throw new TemplateElementNotFoundException("for (" + elementVar + ":" + arrayVar + ")");
    }

    TemplateModel.ForElement assertRootIsFor(TemplateModel model, String arrayVar, String elementVar, String indexVar) {
        if (model.getRoot() instanceof TemplateModel.ForElement) {
            TemplateModel.ForElement forElement = (TemplateModel.ForElement) model.getRoot();
            if (forElement.getElementVarName().equals(elementVar) && forElement.getArrayVarName().equals(arrayVar)
                    && forElement.getIndexVarName().equals(indexVar)) {
                return forElement;
            }
        }
        throw new TemplateElementNotFoundException("for (" + elementVar + ":" + arrayVar + ")");
    }

    TemplateModel.XmlElement assertRootIsXml(TemplateModel.Container element, String tagName) {
        return childrenByType(element, TemplateModel.XmlElement.class)
                .filter(e -> e.getTagName().equals(tagName))
                .findFirst().orElseThrow();
    }

    TemplateModel.IfElement assertContainsIf(TemplateModel.Container element, String condition) {
        return childrenByType(element, TemplateModel.IfElement.class).filter(e -> e.getCondition().equals(condition)).findFirst().orElseThrow();
    }

    TemplateModel.ForElement assertContainsFor(TemplateModel.Container element, String arrayVar, String elementVar) {
        return childrenByType(element, TemplateModel.ForElement.class)
                .filter(e -> e.getElementVarName().equals(elementVar))
                .filter(e -> e.getArrayVarName().equals(arrayVar))
                .findFirst().orElseThrow();
    }

    TemplateModel.ForElement assertContainsFor(TemplateModel.Container element, String arrayVar, String elementVar, String indexVar) {
        return childrenByType(element, TemplateModel.ForElement.class)
                .filter(e -> e.getElementVarName().equals(elementVar))
                .filter(e -> e.getArrayVarName().equals(arrayVar))
                .filter(e -> e.getIndexVarName().equals(indexVar))
                .findFirst().orElseThrow();
    }

    TemplateModel.XmlElement assertContainsXml(TemplateModel.Container element, String tagName) {
        return childrenByType(element, TemplateModel.XmlElement.class)
                .filter(e -> e.getTagName().equals(tagName))
                .findFirst().orElseThrow();
    }

    <E extends TemplateModel.TemplateElement> Stream<E> childrenByType(TemplateModel.Container container, Class<E> type) {
        return container.getElements().stream().filter(type::isInstance).map(type::cast);
    }
}
