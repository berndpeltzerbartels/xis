package one.xis.server;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.DefaultHtmlFile;
import one.xis.HtmlFile;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.NoSuchResourceException;
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

    private final Resources resources;
    private final PathResolver pathResolver;

    private Map<String, Resource> widgetHtmlResources;
    private ResourceCache<String> pageBodyResourceCache;
    private ResourceCache<String> pageHeadResourceCache;
    private ResourceCache<Map<String, String>> pageAttributesResourceCache;

    @XISInit
    void initWidgetResources() {
        widgetHtmlResources = widgetControllers.stream().collect(Collectors.toMap(WidgetUtil::getId, this::htmlResource));
    }

    @XISInit
    void initPageResources() {
        var pageHtmlResources = pageControllers.stream()
                .collect(Collectors.toMap(pathResolver::normalizedPath, this::htmlResource));
        pageHeadResourceCache = new ResourceCache<>(this::extractPageHead, pageHtmlResources);
        pageBodyResourceCache = new ResourceCache<>(this::extractPageBody, pageHtmlResources);
        pageAttributesResourceCache = new ResourceCache<>(this::extractBodyAttributes, pageHtmlResources);
    }


    String getRootPageHtml() {
        return rootPageService.getRootPageHtml();
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
        try {
            return resources.getByPath(getHtmlTemplatePath(controller));
        } catch (NoSuchResourceException e) {
            if (!controller.getClass().isAnnotationPresent(DefaultHtmlFile.class)) {
                throw new RuntimeException("Failed to load HTML template for controller: " + controller.getClass().getName(), e);
            }
            return resources.getByPath(controller.getClass().getAnnotation(DefaultHtmlFile.class).value());
        }
    }

    private String getHtmlTemplatePath(Object controller) {
        var path = new StringBuilder(controller.getClass().getPackageName().replace('.', '/')).append("/");
        if (controller.getClass().isAnnotationPresent(HtmlFile.class)) {
            path.append(controller.getClass().getAnnotation(HtmlFile.class).value());
        } else {
            path.append(controller.getClass().getSimpleName());
        }
        if (!path.toString().endsWith(".html")) {
            path.append(".html");
        }
        return path.toString();
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


}
