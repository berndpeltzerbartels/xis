package one.xis.remote.js2;

import one.xis.template.Container;
import one.xis.template.TemplateElement;
import one.xis.template.TemplateModel;
import one.xis.template.XmlElement;

interface JSParser<T extends TemplateElement> {

    static <E extends TemplateElement> JSLeafParser<E> parser(E element, JSScript result) {
        return null;
    }


    @SuppressWarnings("unchecked")
    static <C extends Container, P extends JSTreeParser<C>> P parser(C element, JSScript result) {
        if (element instanceof TemplateModel) {
            return (P) new JSWidgetParser(result, ((TemplateModel) element).getName());
        }
        if (element instanceof XmlElement) {
            return (P) new JSXmlElementParser(result);
        }
        return null;
    }


}
