package one.xis.js;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.resource.StringResource;

import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class JavascriptProviderImpl implements JavascriptProvider {

    private final JavascriptExtensionLoader extensionLoader;
    private final Resources resources;

    @Getter
    private Resource compressedJavascript;

    @Getter
    private Resource sourceMap;

    @Init
    public void init() {
        this.compressedJavascript = new StringResource("(function() {\n"
                + xisMinJs()
                + extensionJavascript()
                + "\n})();");
        this.sourceMap = resources.getByPath("xis.min.js.map");
    }


    private String xisMinJs() {
        return resources.getByPath("xis.min.js").getContent();
    }

    private String extensionJavascript() {
        Map<String, String> extensions = extensionLoader.loadExtensions();
        if (extensions.isEmpty()) {
            return "";
        }
        return extensions.entrySet().stream()
                .map(entry -> "\n;\n/* " + entry.getKey() + " */\n" + entry.getValue())
                .collect(Collectors.joining());
    }
}
