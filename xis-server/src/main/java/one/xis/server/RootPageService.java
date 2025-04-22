package one.xis.server;

import lombok.Getter;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resources;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;


@XISComponent
class RootPageService {
    private final Resources resources;
    private final GlobalResourcePathProvider globalResourcePathProvider;

    @Getter
    private String rootPageHtml;

    RootPageService(Resources resources, GlobalResourcePathProvider globalResourcePathProvider) {
        this.resources = resources;
        this.globalResourcePathProvider = globalResourcePathProvider;
    }

    @XISInit
    void init() {
        var rootPageHtml = resources.getByPath("index.html").getContent();
        var rootPageDocument = XmlUtil.loadDocument(rootPageHtml);
        var customStaticResourcePath = globalResourcePathProvider.getCustomStaticResourcePath();
        addCssLinks(customStaticResourcePath, rootPageDocument);
        addJsReferences(customStaticResourcePath, rootPageDocument);
        this.rootPageHtml = XmlUtil.asString(rootPageDocument);
    }

    private void addCssLinks(String customStaticResourcePath, Document rootPageDocument) {
        resources.getClassPathResources(customStaticResourcePath, ".css")
                .forEach(resource -> addCssLink(resource.getResourcePath().substring(customStaticResourcePath.length()), rootPageDocument));
    }

    private void addJsReferences(String customStaticResourcePath, Document rootPageDocument) {
        resources.getClassPathResources(customStaticResourcePath, ".js")
                .forEach(resource -> addJsReference(resource.getResourcePath().substring(customStaticResourcePath.length()), rootPageDocument));
    }

    private void addCssLink(String cssPath, Document rootPageDocument) {
        var headElement = rootPageDocument.getElementsByTagName("head").item(0);
        var linkElement = rootPageDocument.createElement("link");
        linkElement.setAttribute("rel", "stylesheet");
        linkElement.setAttribute("type", "text/css");
        linkElement.setAttribute("href", cssPath);
        headElement.appendChild(linkElement);
    }

    private void addJsReference(String jsPath, Document rootPageDocument) {
        var headElement = rootPageDocument.getElementsByTagName("head").item(0);
        var scriptElement = rootPageDocument.createElement("script");
        scriptElement.setAttribute("src", jsPath);
        headElement.appendChild(scriptElement);
    }
}
