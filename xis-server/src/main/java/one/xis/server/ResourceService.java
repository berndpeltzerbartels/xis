package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.Include;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.html.HtmlParser;
import one.xis.html.document.Element;
import one.xis.html.document.HtmlDocument;
import one.xis.resource.GenericResource;
import one.xis.resource.Resource;
import one.xis.resource.ResourceCache;
import one.xis.resource.Resources;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
class ResourceService {

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @XISInject(annotatedWith = Include.class)
    private Collection<Object> includes;

    @XISInject
    private RootPageService rootPageService;

    @XISInject
    private HtmlResourcePathResolver htmlResourcePathResolver;

    private final Resources resources;
    private final PathResolver pathResolver;

    private final HtmlParser htmlParser = new HtmlParser();

    private ResourceCache<Resource> widgetHtmlResourceCache;
    private ResourceCache<Resource> pageBodyResourceCache;
    private ResourceCache<Resource> pageHeadResourceCache;
    private ResourceCache<Resource> pageAttributesResourceCache;
    private ResourceCache<Resource> includeHtmlResourceCache;

    @XISInit
    void initWidgetResources() {
        Map<String, Resource> widgetHtmlResources = widgetControllers.stream()
                .collect(Collectors.toMap(WidgetUtil::getId, this::htmlResource));
        widgetHtmlResourceCache = new ResourceCache<>(r -> r, widgetHtmlResources);
    }

    @XISInit
    void initPageResources() {
        var pageHtmlResources = pageControllers.stream()
                .collect(Collectors.toMap(pathResolver::normalizedPath, this::htmlResource));
        pageHeadResourceCache = new ResourceCache<>(r -> r, pageHtmlResources);
        pageBodyResourceCache = new ResourceCache<>(r -> r, pageHtmlResources);
        pageAttributesResourceCache = new ResourceCache<>(r -> r, pageHtmlResources);
    }

    @XISInit
    void initIncludeResources() {
        var includeHtmlResources = includes.stream()
                .collect(Collectors.toMap(inc -> inc.getClass().getAnnotation(Include.class).value(), this::htmlResource));
        includeHtmlResourceCache = new ResourceCache<>(r -> r, includeHtmlResources);
    }

    String getRootPageHtml() {
        return rootPageService.getRootPageHtml();
    }

    Resource getWidgetHtml(String id) {
        return widgetHtmlResourceCache.getResourceContent(id).orElseThrow();
    }

    Resource getIncludeHtml(String key) {
        return includeHtmlResourceCache.getResourceContent(key).orElseThrow();
    }

    Resource getPageHead(String id) {
        Resource resource = pageHeadResourceCache.getResourceContent(id).orElseThrow();
        String headContent = extractPageHead(resource);
        return new GenericResource<>(headContent, resource.getLastModified(), resource.getResourcePath());
    }

    Resource getPageBody(String id) {
        Resource resource = pageBodyResourceCache.getResourceContent(id).orElseThrow();
        String bodyContent = extractPageBody(resource);
        return new GenericResource<>(bodyContent, resource.getLastModified(), resource.getResourcePath());
    }

    GenericResource<Map<String, String>> getBodyAttributes(String id) {
        Resource resource = pageAttributesResourceCache.getResourceContent(id).orElseThrow();
        Map<String, String> attributes = extractBodyAttributes(resource);
        return new GenericResource<>(attributes, resource.getLastModified(), resource.getResourcePath());
    }

    private String extractPageHead(Resource pageResource) {
        try {
            HtmlDocument doc = htmlParser.parse(pageResource.getContent());
            Element head = doc.getDocumentElement().getElementByTagName("head");
            return head.toHtml();
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract head", e);
        }
    }

    private String extractPageBody(Resource pageResource) {
        try {
            HtmlDocument doc = htmlParser.parse(pageResource.getContent());
            Element body = doc.getDocumentElement().getElementByTagName("body");
            return body.toHtml();
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract body", e);
        }
    }

    private Map<String, String> extractBodyAttributes(Resource pageResource) {
        HtmlDocument doc = htmlParser.parse(pageResource.getContent());
        Element body = doc.getDocumentElement().getElementByTagName("body");
        Map<String, String> map = new LinkedHashMap<>(body.getAttributes());
        return map;
    }

    private Resource htmlResource(Object controller) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        return resources.getByPath(path);
    }


}
