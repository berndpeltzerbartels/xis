package one.xis.remote.js;

import one.xis.template.TemplateElement;

abstract class JavascriptParser {

    abstract JSObject parse();

    static <T extends TemplateElement> JavascriptParser parser(T e, JSScript result) {
        return null;
    }
}
