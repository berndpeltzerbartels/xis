package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.JavascriptExtension;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.resource.Resources;

import java.util.Collection;
import java.util.stream.Collectors;

// TODO : D we need another way to add script extensions ?
@Component
@RequiredArgsConstructor
class JavascriptExtensionService {
    @Inject(annotatedWith = JavascriptExtension.class)
    private Collection<Object> javascriptExtensions;

    @Inject
    private Resources resources;
    private String script = "";

    String getScript() {
        return script;
    }

    @Init
    void createScript() {
        script = javascriptExtensions.stream()
                .map(Object::getClass)
                .map(c -> c.getAnnotation(JavascriptExtension.class))
                .map(JavascriptExtension::value)
                .map(this::loadContent)
                .collect(Collectors.joining("\n"));
    }

    private String loadContent(String resourcePath) {
        return resources.getByPath(resourcePath).getContent();
    }
}
