package one.xis.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;
import one.xis.html.HtmlParser;
import one.xis.html.document.HtmlDocument;
import one.xis.resource.Resource;
import one.xis.resource.Resources;

import java.util.Comparator;
import java.util.LinkedHashSet;


@Component
@RequiredArgsConstructor
class RootPageService {

    private static final String CUSTOM_PUBLIC_RESOURCE_PATH = "public";
    private static final String DEFAULT_ROOT_PAGE = "default-index.html";
    private static final String ROOT_PAGE = "index.html";
    private final Resources resources;
    private final HtmlParser htmlParser = new HtmlParser();
    @Getter
    private String rootPageHtml;

    @Init
    void init() {
        var resourcePath = getRootPageResourcePath();
        var rootPageHtml = resources.getByPath(resourcePath).getContent();
        var rootPageDocument = htmlParser.parse(rootPageHtml);
        addCssLinks(rootPageDocument);
        addJsReferences(rootPageDocument);
        this.rootPageHtml = rootPageDocument.toHtml();
    }

    private void addCssLinks(HtmlDocument rootPageDocument) {
        var cssPaths = new LinkedHashSet<String>();
        resources.getClassPathResources(CUSTOM_PUBLIC_RESOURCE_PATH, ".css")
                .stream()
                .sorted(Comparator.comparingInt(this::cssOrder).thenComparing(Resource::getResourcePath))
                .map(resource -> resource.getResourcePath().substring(CUSTOM_PUBLIC_RESOURCE_PATH.length()))
                .filter(cssPaths::add)
                .forEach(cssPath -> addCssLink(cssPath, rootPageDocument));
    }

    private int cssOrder(Resource resource) {
        return switch (resource.getResourcePath()) {
            case "public/default-theme.css" -> 0;
            case "public/default-main.css" -> 5;
            case "public/xis.css" -> 10;
            case "public/theme.css" -> 100;
            default -> 50;
        };
    }

    private void addJsReferences(HtmlDocument rootPageDocument) {
        resources.getClassPathResources(CUSTOM_PUBLIC_RESOURCE_PATH, ".js")
                .forEach(resource -> addJsReference(resource.getResourcePath().substring(CUSTOM_PUBLIC_RESOURCE_PATH.length()), rootPageDocument));
    }

    private void addCssLink(String cssPath, HtmlDocument rootPageDocument) {
        var headElement = rootPageDocument.getElementsByTagName("head").get(0);
        var linkElement = rootPageDocument.createElement("link");
        linkElement.setAttribute("rel", "stylesheet");
        linkElement.setAttribute("type", "text/css");
        linkElement.setAttribute("href", cssPath);
        headElement.appendChild(linkElement);
    }

    private void addJsReference(String jsPath, HtmlDocument rootPageDocument) {
        var headElement = rootPageDocument.getElementsByTagName("head").get(0);
        var scriptElement = rootPageDocument.createElement("script");
        scriptElement.setAttribute("src", jsPath);
        headElement.appendChild(scriptElement);
    }

    private String getRootPageResourcePath() {
        if (resources.exists(ROOT_PAGE)) {
            return ROOT_PAGE;
        } else if (resources.exists(DEFAULT_ROOT_PAGE)) {
            return DEFAULT_ROOT_PAGE;
        }
        throw new IllegalStateException("No root page found. Please provide an 'index.html' or use the XIS default root page.");
    }


}
