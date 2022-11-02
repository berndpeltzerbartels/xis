package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.context.XISComponent;
import one.xis.path.PathUtils;
import one.xis.resource.ResourceFiles;

@XISComponent
@RequiredArgsConstructor
class PageMetaDataFactory {

    private final ResourceFiles resourceFiles;
    private static int nameIndex;

    PageMetaData createMetaData(Object controller) {
        String path = path(controller);
        return PageMetaData.builder()
                .id(id(path))
                .htmlTemplate(resourceFiles.getByPath(getHtmlTemplatePath(controller)))
                .javascriptClassname(uniqueJavascriptClassName())
                .path(path)
                .welcomePage(isWelcomePage(controller))
                .controllerClass(controllerClass(controller))
                .build();
    }


    private Class<?> controllerClass(Object controller) {
        return controller.getClass();
    }

    private String path(Object controller) {
        String path = controller.getClass().getAnnotation(one.xis.Page.class).path();
        if (PathUtils.hasSuffix(path)) {
            String suffix = PathUtils.getSuffix(path);
            if (!suffix.equals("html")) {
                throw new IllegalStateException(controller.getClass() + ": illegal suffix in path-attribute of @Page-annotation: " + suffix + ". Suffix must be empty or '.html'");
            }
            return path;
        }
        return path + ".html";
    }

    private boolean isWelcomePage(Object controller) {
        return controller.getClass().getAnnotation(Page.class).welcomePage();
    }

    public String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }

    private String uniqueJavascriptClassName() {
        return "P" + nameIndex++;
    }

    protected String id(String path) {
        return pathToUrn(path);
    }

    public static String pathToUrn(String path) {
        return "page" + path.replace('/', ':');
    }
}
