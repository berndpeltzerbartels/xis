package one.xis.remote.js;

import one.xis.template.TemplateElement;

abstract class JavascriptParser<T extends TemplateElement> {

    abstract JSObject parse(T e);

    static <T extends TemplateElement> JavascriptParser<T> parser(T e) {
        return null;
    }
}
