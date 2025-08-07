package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.resource.Resources;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;


@XISComponent
@RequiredArgsConstructor
class RootPageService {

    private static final String CUSTOM_PUBLIC_RESOURCE_PATH = "/public";
    private static final String DEFAULT_ROOT_PAGE = "default-index.html";
    private static final String CUSTOM_ROOT_PAGE = "index.html";


    private final Resources resources;

    @Getter
    private String rootPageHtml;

    @XISInit
    void init() {
        var resourcePath = resources.exists(CUSTOM_ROOT_PAGE) ? CUSTOM_ROOT_PAGE : DEFAULT_ROOT_PAGE;
        var rootPageHtml = resources.getByPath(resourcePath).getContent();
        var rootPageDocument = XmlUtil.loadDocument(rootPageHtml);
        addCssLinks(rootPageDocument);
        addJsReferences(rootPageDocument);
        this.rootPageHtml = XmlUtil.asString(rootPageDocument);
    }

    private void addCssLinks(Document rootPageDocument) {
        resources.getClassPathResources(CUSTOM_PUBLIC_RESOURCE_PATH, ".css")
                .forEach(resource -> addCssLink(resource.getResourcePath().substring(CUSTOM_PUBLIC_RESOURCE_PATH.length()), rootPageDocument));
    }

    private void addJsReferences(Document rootPageDocument) {
        resources.getClassPathResources(CUSTOM_PUBLIC_RESOURCE_PATH, ".js")
                .forEach(resource -> addJsReference(resource.getResourcePath().substring(CUSTOM_PUBLIC_RESOURCE_PATH.length()), rootPageDocument));
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
