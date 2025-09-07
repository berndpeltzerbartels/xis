package one.xis.js;

import lombok.Getter;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.resource.StringResource;

@Getter
public enum JavascriptSource {
    EVENT_REGISTRY(getResource("event-registry.js")),
    CLASSES(getResource("classes.js")),
    FUNCTIONS(getResource("functions.js")),
    TEST(getResource("test.js")),
    TEST_MAIN(getResource("test-main.js")),
    TEST_APP_INSTANCE(new StringResource("var app = new TestApplication();"));

    private final String content;
    private final Resource resource;

    JavascriptSource(Resource resource) {
        this.resource = resource;
        this.content = resource.getContent();
    }

    static Resource getResource(String path) {
        return new Resources().getByPath(path);
    }
}
