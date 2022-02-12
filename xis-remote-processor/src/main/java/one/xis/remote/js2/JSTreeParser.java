package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;
import one.xis.template1.Container;
import one.xis.template1.TemplateElement;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
abstract class JSTreeParser<C extends Container> implements JSParser<C> {
    private final JSScript script;

    protected abstract JSObjectDeclaration parse(C element, List<String> childNames);

    JSObjectDeclaration parse(C container) {
        return parseContainer(container);
    }

    private JSObjectDeclaration doParse(TemplateElement element) {
        if (element instanceof Container) {
            return parseContainer((Container) element);
        }
        return parseElement(element);
    }

    private JSObjectDeclaration parseContainer(Container element) {
        List<String> childNames = names(parseChildren(element));
        JSTreeParser<Container> parser = JSParser.parser(element, script);
        return parser.parse(element, childNames);
    }

    private JSObjectDeclaration parseElement(TemplateElement element) {
        return JSParser.parser(element, script).parse(element);
    }

    private List<JSObjectDeclaration> parseChildren(Container container) {
        return container.getElements().stream().map(this::doParse).collect(Collectors.toList());
    }

    private List<String> names(List<JSObjectDeclaration> declarations) {
        return declarations.stream().map(JSObjectDeclaration::getName).collect(Collectors.toList());
    }
}
