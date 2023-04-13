package one.xis.context;


import lombok.Getter;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.server.FrontendService;
import one.xis.test.dom.Document;
import one.xis.test.js.JSUtil;
import one.xis.test.js.LocalStorage;

import javax.script.CompiledScript;
import javax.script.ScriptException;
import java.util.*;
import java.util.stream.Collectors;

public class IntegrationTestContext {

    @Getter
    private final Document document;

    @Getter
    private final LocalStorage localStorage;
    private final AppContext internalContext;
    private final Resources resources;
    private final CompiledScript compiledScript;
    private final FrontendService frontendService;

    public static Builder builder() {
        return new Builder();
    }

    public IntegrationTestContext(Object... controllers) {
        this.localStorage = new LocalStorage();
        this.resources = new Resources();
        internalContext = AppContextBuilder.createInstance()
                .withPackage("one.xis")
                .withSingeltons(controllers)
                .build();
        frontendService = internalContext.getSingleton(FrontendService.class);
        document = Document.of(frontendService.getRootPageHtml());
        compiledScript = compiledScript(getApiJavascript());
    }

    public void openPage(String uri, Map<String, Object> parameters) {
        document.location.pathname = uri;
        try {
            compiledScript.eval();
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    public void openPage(String uri) {
        openPage(uri, Collections.emptyMap());
    }

    private CompiledScript compiledScript(String javascript) {
        var bindings = new HashMap<String, Object>();
        bindings.put("frontendServiceWrapper", new FrontendServiceWrapper(frontendService));
        bindings.put("localStorage", localStorage);
        bindings.put("document", document);
        try {
            return JSUtil.compile(javascript, bindings);
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }


    private String getApiJavascript() {
        return resources.getClassPathResources("js", ".js").stream()
                .filter(resource -> !resource.getResourcePath().endsWith("HttpClient.js")) // we have a mock instead
                .map(Resource::getContent)
                .collect(Collectors.joining("\n"));
    }

    public static class Builder {

        private final Collection<Object> singeltons = new HashSet<>();

        public Builder withSingelton(Object o) {
            singeltons.add(o);
            return this;
        }

        public Builder withMock(Object o) {
            return withSingelton(o);
        }

        public IntegrationTestContext build() {
            return new IntegrationTestContext(singeltons.stream().toArray(Object[]::new));
        }
    }


}
