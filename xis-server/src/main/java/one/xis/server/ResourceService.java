package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.Include;
import one.xis.Modal;
import one.xis.Page;
import one.xis.Frontlet;
import one.xis.Route;
import one.xis.Router;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.context.Inject;
import one.xis.html.HtmlParser;
import one.xis.html.document.Element;
import one.xis.html.document.HtmlDocument;
import one.xis.resource.GenericResource;
import one.xis.resource.Resource;
import one.xis.resource.ResourceCache;
import one.xis.resource.Resources;
import one.xis.utils.lang.MethodUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class ResourceService {

    private final Resources resources;
    private final PathResolver pathResolver;
    private final HtmlParser htmlParser = new HtmlParser();
    @Inject(annotatedWith = Frontlet.class)
    private Collection<Object> frontletControllers;
    @Inject(annotatedWith = Modal.class)
    private Collection<Object> modalControllers;
    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;
    @Inject(annotatedWith = Router.class)
    private Collection<Object> routerControllers;
    @Inject(annotatedWith = Include.class)
    private Collection<Object> includes;
    @Inject
    private RootPageService rootPageService;
    @Inject
    private HtmlResourcePathResolver htmlResourcePathResolver;
    private Map<String, HtmlDocumentResource> frontletDocumentCache;
    private Map<String, HtmlDocumentResource> pageDocumentCache;
    private ResourceCache<Resource> includeHtmlResourceCache;

    @Init
    void initFrontletResources() {
        var controllers = new java.util.ArrayList<Object>();
        controllers.addAll(frontletControllers);
        controllers.addAll(modalControllers);
        frontletDocumentCache = controllers.stream()
                .collect(Collectors.toMap(
                        FrontletUtil::getId,
                        controller -> htmlDocumentResource(controller, "Frontlet")
                ));
    }

    @Init
    void initPageResources() {
        pageDocumentCache = pageControllers.stream()
                .collect(Collectors.toMap(
                        pathResolver::normalizedPath,
                        controller -> htmlDocumentResource(controller, "Page")
        ));
        for (Object routerController : routerControllers) {
            for (var method : MethodUtils.allMethods(routerController)) {
                if (method.isAnnotationPresent(Route.class)) {
                    var path = pathResolver.createPath(RouterUtil.getRouteUrl(routerController, method)).normalized();
                    pageDocumentCache.put(path, emptyRouterPageResource(path));
                }
            }
        }
    }

    @Init
    void initIncludeResources() {
        var includeHtmlResources = includes.stream()
                .collect(Collectors.toMap(
                        inc -> inc.getClass().getAnnotation(Include.class).value(),
                        this::htmlResource
                ));
        includeHtmlResourceCache = new ResourceCache<>(r -> r, includeHtmlResources);
    }

    String getRootPageHtml() {
        return rootPageService.getRootPageHtml();
    }

    Resource getFrontletHtml(String id) {
        HtmlDocumentResource docResource = frontletDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Frontlet not found: " + id);
        return new GenericResource<>(docResource.getObjectContent().toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    Resource getIncludeHtml(String key) {
        return includeHtmlResourceCache.getResourceContent(key).orElseThrow();
    }

    Resource getPageHead(String id) {
        HtmlDocumentResource docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element head = docResource.getObjectContent().getDocumentElement().getElementByTagName("head");
        return new GenericResource<>(head.toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    Resource getPageBody(String id) {
        HtmlDocumentResource docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element body = docResource.getObjectContent().getDocumentElement().getElementByTagName("body");
        return new GenericResource<>(body.toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    GenericResource<Map<String, String>> getBodyAttributes(String id) {
        HtmlDocumentResource docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element body = docResource.getObjectContent().getDocumentElement().getElementByTagName("body");
        return new GenericResource<>(new LinkedHashMap<>(body.getAttributes()), docResource.getLastModified(), docResource.getResourcePath());
    }

    private HtmlDocumentResource htmlDocumentResource(Object controller, String type) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        var resource = resources.getByPath(path);
        return new HtmlDocumentResource(resource, htmlParser, controller.getClass(), type);
    }

    private HtmlDocumentResource emptyRouterPageResource(String path) {
        var doc = htmlParser.parse("<!DOCTYPE html><html><head><title></title></head><body></body></html>");
        return new HtmlDocumentResource(doc, "router:" + path);
    }

    private Resource htmlResource(Object controller) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        return resources.getByPath(path);
    }
}
