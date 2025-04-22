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
    private final ResourcePathProvider resourcePathProvider;

    @Getter
    private String rootPageHtml;

    RootPageService(Resources resources, ResourcePathProvider resourcePathProvider) {
        this.resources = resources;
        this.resourcePathProvider = resourcePathProvider;
    }

    @XISInit
    void init() {
        var rootPageHtml = resources.getByPath("index.html").getContent();
        var rootPageDocument = XmlUtil.loadDocument(rootPageHtml);
        var customGlobalResourcePath = resourcePathProvider.getCustomStaticResourcePath() + "/global";
        addCssLinks(customGlobalResourcePath, rootPageDocument);
        addJsReferences(customGlobalResourcePath, rootPageDocument);
        this.rootPageHtml = XmlUtil.asString(rootPageDocument);
    }

    private void addCssLinks(String customStaticResourcePath, Document rootPageDocument) {
        resources.getClassPathResources(customStaticResourcePath, ".css")
                .forEach(resource -> addCssLink(resource.getResourcePath().substring(customStaticResourcePath.length()), rootPageDocument));
    }

    private void addJsReferences(String customGlobalResourcePath, Document rootPageDocument) {
        resources.getClassPathResources(customGlobalResourcePath, ".js")
                .forEach(resource -> addJsReference(resource.getResourcePath().substring(resourcePathProvider.getCustomStaticResourcePath().length()), rootPageDocument));
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
