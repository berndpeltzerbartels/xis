package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.utils.xml.XmlUtil;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.tinylog.Logger;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
class HtmlResourceService {

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    private final Resources resources;

    private Map<String, Resource> widgetHtmlResources;
    private Map<String, Resource> pageHtmlResources;
    private ResourceCache<String> pageBodyResourceCache;
    private ResourceCache<String> pageHeadResourceCache;
    private ResourceCache<Map<String, String>> pageAttributesResourceCache;

    private Resource rootPage;

    @XISInit
    void initWidgetResources() {
        widgetHtmlResources = widgetControllers.stream().collect(Collectors.toMap(WidgetUtil::getId, this::htmlResource));
    }

    @XISInit
    void initPageResources() {
        pageHtmlResources = pageControllers.stream().collect(Collectors.toMap(contr -> contr.getClass().getAnnotation(Page.class).value(), this::htmlResource));
        pageHeadResourceCache = new ResourceCache<String>(this::extractPageHead, pageHtmlResources);
        pageBodyResourceCache = new ResourceCache<String>(this::extractPageBody, pageHtmlResources);
        pageAttributesResourceCache = new ResourceCache<Map<String, String>>(this::extractBodyAttributes, pageHtmlResources);

    }

    @XISInit
    void initRootPage() {
        rootPage = resources.getByPath("/index.html");
    }


    String getRootPageHtml() {
        return rootPage.getContent();
    }

    String getWidgetHtml(String id) {
        return widgetHtmlResources.get(id).getContent();
    }

    String getPageHead(String id) {
        return pageHeadResourceCache.getResourceContent(id).orElseThrow();
    }

    String getPageBody(String id) {
        return pageBodyResourceCache.getResourceContent(id).orElseThrow();
    }

    Map<String, String> getBodyAttributes(String id) {
        return pageAttributesResourceCache.getResourceContent(id).orElseThrow();
    }

    private String extractPageHead(Resource pageResource) {
        try {
            var content = pageResource.getContent();
            Logger.info("content for head :" + content);
            var doc = createDocument(content);
            var head = doc.getRootElement().element("head");
            return serialize(head);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract head", e);
        }
    }

    private String extractPageBody(Resource pageResource) {
        try {
            var content = pageResource.getContent();
            Logger.info("content for body :" + content);
            var doc = createDocument(content);
            var body = doc.getRootElement().element("body");
            return serialize(body);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract body", e);
        }
    }

    private Map<String, String> extractBodyAttributes(Resource pageResource) {
        var content = pageResource.getContent();
        var doc = XmlUtil.loadDocument(content);
        return XmlUtil.getElementByTagName(doc.getDocumentElement(), "body").map(XmlUtil::getAttributes).orElse(Collections.emptyMap());
    }


    private Resource htmlResource(Object controller) {
        return resources.getByPath(getHtmlTemplatePath(controller));
    }

    private String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }

    private Document createDocument(String xml) {
        try {
            return new SAXReader().read(new StringReader(xml));
        } catch (DocumentException e) {
            throw new RuntimeException("Failed to read document:\n" + xml, e);
        }
    }

    @SneakyThrows
    private String serialize(org.dom4j.Element element) {
        StringWriter stringWriter = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter();
        xmlWriter.setWriter(stringWriter);
        try {
            xmlWriter.write(element);
        } catch (Exception e) {
            throw new RuntimeException("", e);
        } finally {
            xmlWriter.close();
        }
        return stringWriter.toString();
    }

    @RequiredArgsConstructor
    static class ResourceCache<T> {
        private final Function<Resource, T> updateFunction;
        private final Map<String, Resource> resources;
        private final Map<String, T> cache = new HashMap<>();


        Optional<T> getResourceContent(String id) {
            if (!resources.containsKey(id)) {
                return Optional.empty();
            }
            var resource = resources.get(id);
            if (!cache.containsKey(id) || resource.isObsolete()) {
                return Optional.of(cache.computeIfAbsent(id, key -> updateFunction.apply(resources.get(key))));
            }
            return Optional.of(cache.get(id));
        }

    }
}
