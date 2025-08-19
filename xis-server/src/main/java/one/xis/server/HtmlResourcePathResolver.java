package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.DefaultHtmlFile;
import one.xis.HtmlFile;
import one.xis.context.XISComponent;
import one.xis.resource.Resources;

@XISComponent
@RequiredArgsConstructor
class HtmlResourcePathResolver {

    private final Resources resources;

    String htmlResourcePath(Class<?> controllerClazz) {
        if (controllerClazz.isAnnotationPresent(HtmlFile.class)) {
            var path = controllerClazz.getAnnotation(HtmlFile.class).value();
            var translatedPath = translateHtmlPath(path, controllerClazz);
            if (resources.exists(translatedPath)) {
                return translatedPath;
            }
        }
        if (controllerClazz.isAnnotationPresent(DefaultHtmlFile.class)) {
            var path = controllerClazz.getAnnotation(DefaultHtmlFile.class).value();
            var translatedPath = translateHtmlPath(path, controllerClazz);
            if (resources.exists(translatedPath)) {
                return translatedPath;
            }
        }
        return htmlResourcePathByControllerClass(controllerClazz);
    }

    private String htmlResourcePathByControllerClass(Class<?> controllerClazz) {
        return htmlPathRelativeToPackage(controllerClazz.getSimpleName(), controllerClazz);
    }


    private String translateHtmlPath(String path, Class<?> controllerClazz) {
        if (path.startsWith("/")) {
            return absoluteHtmlPathToResource(path);
        }
        return htmlPathRelativeToPackage(path, controllerClazz);
    }

    /**
     * Generates the HTML path for a controller class.
     * The path is based on the package name and the class name, formatted as "package/name/ClassName.html".
     *
     * @param controllerClazz the controller class
     * @return the HTML path
     */
    private String htmlPathRelativeToPackage(String path, Class<?> controllerClazz) {
        var packageName = controllerClazz.getPackageName();
        var resultPath = packageName.replace('.', '/') + "/" + path;
        if (!resultPath.endsWith(".html")) {
            resultPath += ".html";
        }
        return resultPath;
    }

    private String absoluteHtmlPathToResource(String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (!path.endsWith(".html")) {
            path += ".html";
        }
        return path;
    }
}
