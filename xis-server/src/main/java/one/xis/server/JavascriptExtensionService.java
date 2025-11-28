package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.JavascriptExtension;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.Resources;

import java.util.Collection;
import java.util.stream.Collectors;

// TODO : D we need another way to add script extensions ?
@XISComponent
@RequiredArgsConstructor
class JavascriptExtensionService {
    @XISInject(annotatedWith = JavascriptExtension.class)
    private Collection<Object> javascriptExtensions;

    @XISInject
    private Resources resources;
    private String script = "";

    String getScript() {
        return script;
    }

    @XISInit
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
