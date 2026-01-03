package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.Include;
import one.xis.Page;
import one.xis.Widget;
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

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class ResourceService {

    @Inject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @Inject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @Inject(annotatedWith = Include.class)
    private Collection<Object> includes;

    @Inject
    private RootPageService rootPageService;

    @Inject
    private HtmlResourcePathResolver htmlResourcePathResolver;

    private final Resources resources;
    private final PathResolver pathResolver;

    private final HtmlParser htmlParser = new HtmlParser();

    private Map<String, GenericResource<HtmlDocument>> widgetDocumentCache;
    private Map<String, GenericResource<HtmlDocument>> pageDocumentCache;
    private ResourceCache<Resource> includeHtmlResourceCache;

    @Init
    void initWidgetResources() {
        widgetDocumentCache = widgetControllers.stream()
                .collect(Collectors.toMap(
                        WidgetUtil::getId,
                        controller -> {
                            HtmlDocument doc = parseAndValidate(controller, "Widget");
                            return toResource(controller, doc);
                        }
                ));
    }

    @Init
    void initPageResources() {
        pageDocumentCache = pageControllers.stream()
                .collect(Collectors.toMap(
                        pathResolver::normalizedPath,
                        controller -> {
                            HtmlDocument doc = parseAndValidate(controller, "Page");
                            return toResource(controller, doc);
                        }
                ));
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

    Resource getWidgetHtml(String id) {
        GenericResource<HtmlDocument> docResource = widgetDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Widget not found: " + id);
        return new GenericResource<>(docResource.getObjectContent().toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    Resource getIncludeHtml(String key) {
        return includeHtmlResourceCache.getResourceContent(key).orElseThrow();
    }

    Resource getPageHead(String id) {
        GenericResource<HtmlDocument> docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element head = docResource.getObjectContent().getDocumentElement().getElementByTagName("head");
        return new GenericResource<>(head.toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    Resource getPageBody(String id) {
        GenericResource<HtmlDocument> docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element body = docResource.getObjectContent().getDocumentElement().getElementByTagName("body");
        return new GenericResource<>(body.toHtml(), docResource.getLastModified(), docResource.getResourcePath());
    }

    GenericResource<Map<String, String>> getBodyAttributes(String id) {
        GenericResource<HtmlDocument> docResource = pageDocumentCache.get(id);
        if (docResource == null) throw new IllegalArgumentException("Page not found: " + id);
        Element body = docResource.getObjectContent().getDocumentElement().getElementByTagName("body");
        return new GenericResource<>(new LinkedHashMap<>(body.getAttributes()), docResource.getLastModified(), docResource.getResourcePath());
    }

    private HtmlDocument parseAndValidate(Object controller, String type) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        var resource = resources.getByPath(path);
        try {
            return htmlParser.parse(resource.getContent());
        } catch (Exception e) {
            throw new IllegalStateException(
                    String.format("%s template parsing failed: %s (%s)\n  Controller: %s\n  Path: %s",
                            type,
                            e.getMessage(),
                            e.getClass().getSimpleName(),
                            controller.getClass().getName(),
                            path),
                    e
            );
        }
    }

    private GenericResource<HtmlDocument> toResource(Object controller, HtmlDocument doc) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        var resource = resources.getByPath(path);
        return new GenericResource<>(doc, resource.getLastModified(), resource.getResourcePath());
    }

    private Resource htmlResource(Object controller) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        return resources.getByPath(path);
    }
}
