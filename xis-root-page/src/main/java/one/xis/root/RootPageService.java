package one.xis.root;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.resource.ReloadableResourceFile;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;

import java.util.Collection;
import java.util.Set;

@XISComponent
@RequiredArgsConstructor
public class RootPageService {

    private final ResourceFiles resourceFiles;
    private final InitializerScript initializerScript;

    private static final Collection<String> RESOURCES = Set.of("functions.js", "classes1.js", "classes2.js", "classes3.js", "xis-globals.js", "custom-script.js");
    private RootPage rootPage;

    public void createRootContent() {
        rootPage.createContent();
    }

    public String getRootPageHtml() {
        return rootPage.getContent();
    }

    public ResourceFile getJavascriptResource(String file) {
        if (!RESOURCES.contains(file)) {
            throw new IllegalArgumentException("not a resource: " + file);
        }
        if (file.equals("custom-script.js")) {
            return getCustomJavascript();
        }
        return resourceFiles.getByPath("js/" + file);
    }

    public String getInitializerScipt() {
        return initializerScript.getContent();
    }

    public ResourceFile getCustomJavascript() {
        if (resourceFiles.exists(RootPage.CUSTOM_SCRIPT)) {
            ResourceFile customScript = resourceFiles.getByPath(RootPage.CUSTOM_SCRIPT);
            if (customScript instanceof ReloadableResourceFile) {
                ReloadableResourceFile reloadableResourceFile = (ReloadableResourceFile) customScript;
                if (reloadableResourceFile.isObsolete()) {
                    reloadableResourceFile.reload();
                }
            }
            return customScript;
        }
        return ResourceFile.EMPTY_FILE;
    }

}
