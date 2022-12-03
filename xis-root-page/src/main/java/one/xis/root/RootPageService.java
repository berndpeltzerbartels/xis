package one.xis.root;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ReloadableResource;
import one.xis.resource.Resource;
import one.xis.resource.Resources;

import java.util.Collection;
import java.util.Set;

@XISComponent
@RequiredArgsConstructor
public class RootPageService {

    private final Resources resources;
    private final InitializerScript initializerScript;
    private final RootPage rootPage;

    private static final Collection<String> JS_RESOURCES = Set.of("xis.js", "xis-globals.js", "custom-script.js");


    public void createRootContent() {
        rootPage.createContent();
    }

    public String getRootPageHtml() {
        return rootPage.getContent();
    }

    public Resource getJavascriptResource(String file) {
        if (!JS_RESOURCES.contains(file)) {
            throw new IllegalArgumentException("forbidden resource: " + file);
        }
        if (file.equals("custom-script.js")) {
            return getCustomJavascript();
        }
        return resources.getByPath("js/" + file);
    }

    public String getInitializerScipt() {
        return initializerScript.getContent();
    }

    public Resource getCustomJavascript() {
        if (resources.exists(RootPage.CUSTOM_SCRIPT)) {
            Resource customScript = resources.getByPath(RootPage.CUSTOM_SCRIPT);
            if (customScript instanceof ReloadableResource) {
                ReloadableResource reloadableResourceFile = (ReloadableResource) customScript;
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                }
            }
            return customScript;
        }
        return Resource.EMPTY;
    }

}
