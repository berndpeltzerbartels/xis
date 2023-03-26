package one.xis.page;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Welcome;
import one.xis.context.XISComponent;
import one.xis.path.PathUtils;
import one.xis.resource.Resources;

@XISComponent
@RequiredArgsConstructor
class PageMetaDataFactory {

    private final Resources resources;
    private static int nameIndex;

    PageMetaData createMetaData(Class<?> controllerClass) {
        String path = path(controllerClass);
        return PageMetaData.builder()
                .htmlTemplate(resources.getByPath(getHtmlTemplatePath(controllerClass)))
                .javascriptClassname(uniqueJavascriptClassName())
                .path(path)
                .welcomePage(isWelcomePage(controllerClass))
                .controllerClass(controllerClass)
                .build();
    }

    private String path(Class<?> controllerClass) {
        String path = controllerClass.getAnnotation(Page.class).value();
        if (PathUtils.hasSuffix(path)) {
            String suffix = PathUtils.getSuffix(path);
            if (!suffix.equals("html")) {
                throw new IllegalStateException(controllerClass + ": illegal suffix in path-attribute of @Page-annotation: " + suffix + ". Suffix must be empty or '.html'");
            }
            return path;
        }
        return path + ".html";
    }

    private boolean isWelcomePage(Class<?> controllerClass) {
        return controllerClass.isAnnotationPresent(Welcome.class);
    }

    public String getHtmlTemplatePath(Class<?> controllerClass) {
        return controllerClass.getName().replace('.', '/') + ".html";
    }

    private String uniqueJavascriptClassName() {
        return "P" + nameIndex++;
    }

}
