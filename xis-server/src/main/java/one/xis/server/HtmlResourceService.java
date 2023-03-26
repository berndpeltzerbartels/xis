package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.Resource;
import one.xis.resource.Resources;
import one.xis.utils.xml.XmlUtil;
import org.tinylog.Logger;
import org.w3c.dom.Element;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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

    private Resource rootPage;

    @XISInit
    void initWidgetResources() {
        widgetHtmlResources = widgetControllers.stream().collect(Collectors.toMap(WidgetUtil::getId, this::htmlResource));
    }

    @XISInit
    void initPageResources() {
        pageHtmlResources = pageControllers.stream().collect(Collectors.toMap(contr -> contr.getClass().getAnnotation(Page.class).value(), this::htmlResource));
    }

    @XISInit
    void initRootPage() {
        rootPage = resources.getByPath("/public/index.html");
    }


    String getRootPageHtml() {
        return rootPage.getContent();
    }

    String getWidgetHtml(String id) {
        return widgetHtmlResources.get(id).getContent();
    }

    String getPageHead(String id) {
        try {
            var content = pageHtmlResources.get(id).getContent();
            Logger.info("content for head :" + content);
            var doc = XmlUtil.loadDocument(content);

            return XmlUtil.getElementByTagName(doc.getDocumentElement(), "head").map(this::childNodesAsString).orElse("");
        } catch (Exception e) {
            throw new RuntimeException("Unable to load head for " + id, e);
        }
    }

    String getPageBody(String id) {
        var content = pageHtmlResources.get(id).getContent();
        Logger.info("content for body:" + content);
        var doc = XmlUtil.loadDocument(content);
        return XmlUtil.getElementByTagName(doc.getDocumentElement(), "body").map(this::childNodesAsString).orElse("");
    }

    Map<String, String> getBodyAttributes(String id) {
        var content = pageHtmlResources.get(id).getContent();
        var doc = XmlUtil.loadDocument(content);
        return XmlUtil.getElementByTagName(doc.getDocumentElement(), "body").map(XmlUtil::getAttributes).orElse(Collections.emptyMap());
    }


    private String childNodesAsString(Element e) {
        return XmlUtil.getChildNodes(e).map(XmlUtil::asString).collect(Collectors.joining()).trim();
    }

    private Resource htmlResource(Object controller) {
        return resources.getByPath(getHtmlTemplatePath(controller));
    }

    private String getHtmlTemplatePath(Object controller) {
        return controller.getClass().getName().replace('.', '/') + ".html";
    }
}
