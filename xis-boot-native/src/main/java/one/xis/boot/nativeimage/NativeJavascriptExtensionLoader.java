package one.xis.boot.nativeimage;

import one.xis.context.Component;
import one.xis.js.JavascriptExtensionLoader;
import one.xis.resource.Resources;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NativeJavascriptExtensionLoader extends JavascriptExtensionLoader {

    private static final String EXTENSION_INDEX = "META-INF/xis/js/extensions";

    private final Resources resources;

    public NativeJavascriptExtensionLoader(Resources resources) {
        this.resources = resources;
    }

    @Override
    public Map<String, String> loadExtensions() {
        var extensions = new LinkedHashMap<String, String>();
        if (!resources.exists(EXTENSION_INDEX)) {
            return extensions;
        }
        Arrays.stream(resources.getByPath(EXTENSION_INDEX).getContent().split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isBlank() && !line.startsWith("#"))
                .forEach(path -> extensions.put(path, resources.getByPath(path).getContent()));
        return extensions;
    }
}
