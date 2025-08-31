package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.GenericResource;
import one.xis.resource.Resource;
import one.xis.resource.ResourceCache;
import one.xis.resource.Resources;
import one.xis.utils.xml.XmlUtil;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@XISComponent
@RequiredArgsConstructor
class ResourceService {

    @XISInject(annotatedWith = Widget.class)
    private Collection<Object> widgetControllers;

    @XISInject(annotatedWith = Page.class)
    private Collection<Object> pageControllers;

    @XISInject
    private RootPageService rootPageService;

    @XISInject
    private HtmlResourcePathResolver htmlResourcePathResolver;

    private final Resources resources;
    private final PathResolver pathResolver;

    private ResourceCache<Resource> widgetHtmlResourceCache;
    private ResourceCache<Resource> pageBodyResourceCache;
    private ResourceCache<Resource> pageHeadResourceCache;
    private ResourceCache<Resource> pageAttributesResourceCache;

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


    String getRootPageHtml() {
        return rootPageService.getRootPageHtml();
    }

    Resource getWidgetHtml(String id) {
        return widgetHtmlResourceCache.getResourceContent(id).orElseThrow();
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
            var content = pageResource.getContent();
            var doc = createDocument(content);
            var html = doc.getRootElement();
            var head = doc.getRootElement().element("head");
            html.remove(head);
            if (head == null) {
                throw new IllegalStateException("page must contain head element");
            }
            return toTemplateString(head);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract head", e);
        }
    }

    private String extractPageBody(Resource pageResource) {
        try {
            var content = pageResource.getContent();
            var doc = createDocument(content);
            var html = doc.getRootElement();
            var body = doc.getRootElement().element("body");
            if (body == null) {
                throw new IllegalStateException("page must contain body element");
            }
            html.remove(body);
            return toTemplateString(body);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract body", e);
        }
    }

    @SuppressWarnings("unchecked")
    private String toTemplateString(Element element) {
        fixScriptElements(element);
        var templateDoc = DocumentHelper.createDocument();
        var templateElement = DocumentHelper.createElement("xis:template");
        templateDoc.add(templateElement);

        List<Node> children = new ArrayList<>(element.content()); // nicht element.elements()
        for (Node child : children) {
            element.remove(child);
            if (child instanceof org.dom4j.Text textNode) {
                if (textNode.getText().trim().isEmpty()) {
                    continue; // remove empty text nodes
                }
            }
            templateElement.add(child);
        }
        return serialize(templateElement);
    }

    private void fixScriptElements(Element element) {
        if ("script".equalsIgnoreCase(element.getName()) && element.content().isEmpty()) {
            element.addText("");
        }
        for (Iterator<?> it = element.elementIterator(); it.hasNext(); ) {
            fixScriptElements((Element) it.next());
        }
    }

    private Map<String, String> extractBodyAttributes(Resource pageResource) {
        var content = pageResource.getContent();
        var doc = XmlUtil.loadDocument(content);
        return XmlUtil.getElementByTagName(doc.getDocumentElement(), "body").map(XmlUtil::getAttributes).orElse(Collections.emptyMap());
    }


    private Resource htmlResource(Object controller) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        return resources.getByPath(path);
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
        XMLWriter xmlWriter = new HtmlWriter();
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


}
