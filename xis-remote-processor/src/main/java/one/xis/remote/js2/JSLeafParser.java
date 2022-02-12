package one.xis.remote.js2;

import one.xis.template1.TemplateElement;


interface JSLeafParser<T extends TemplateElement> extends JSParser<T> {
    JSObjectDeclaration parse(T element);
}
