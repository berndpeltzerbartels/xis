package one.xis.server;

import lombok.RequiredArgsConstructor;
import one.xis.Page;
import one.xis.Widget;
import one.xis.context.XISComponent;
import one.xis.context.XISInit;
import one.xis.context.XISInject;
import one.xis.resource.GenericResource;
import one.xis.resource.Resource;
import one.xis.resource.ResourceCache;
import one.xis.resource.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Parser;
import org.jsoup.parser.Tag;

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

    // ---------- jsoup-basierte Extraktion ----------

    private String extractPageHead(Resource pageResource) {
        try {
            Document doc = parse(pageResource.getContent());
            Element head = doc.head();
            return toTemplateString(head);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract head", e);
        }
    }

    private String extractPageBody(Resource pageResource) {
        try {
            Document doc = parse(pageResource.getContent());
            Element body = doc.body();
            return toTemplateString(body);
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract body", e);
        }
    }

    private Map<String, String> extractBodyAttributes(Resource pageResource) {
        Document doc = parse(pageResource.getContent());
        Element body = doc.body();
        Map<String, String> map = new LinkedHashMap<>();
        body.attributes().forEach(attr -> map.put(attr.getKey(), attr.getValue()));
        return map;
    }

    private Resource htmlResource(Object controller) {
        var path = htmlResourcePathResolver.htmlResourcePath(controller.getClass());
        return resources.getByPath(path);
    }

    // ---------- Helpers ----------

    /**
     * Robuster HTML5-Parser, tolerant gegenüber nicht geschlossenen Tags.
     */
    private Document parse(String html) {
        // htmlParser() = HTML5-Regeln (tolerant). baseUri leer, damit relative URLs unverändert bleiben.
        Document doc = Jsoup.parse(html, "", Parser.htmlParser());
        // Ausgabe-Einstellungen: kein Pretty-Print, HTML-Syntax
        doc.outputSettings(new Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html));
        return doc;
    }

    /**
     * Baut <xis:template>…</xis:template> und hängt alle Kinder (inkl. Text/Kommentare) von 'host' hinein.
     * Leere <script>-Tags werden zu <script></script>.
     */
    private String toTemplateString(Element host) {
        Document templateDoc = new Document("");
        templateDoc.outputSettings(new Document.OutputSettings()
                .prettyPrint(false)
                .syntax(Document.OutputSettings.Syntax.html));

        Element templateEl = new Element(Tag.valueOf("xis:template"), "");
        templateDoc.appendChild(templateEl);

        // Alle Child-Nodes (auch Text/Comments) übernehmen; leere Whitespaces weglassen
        for (Node n : host.childNodes()) {
            if (n instanceof TextNode t && t.isBlank()) continue;
            templateEl.appendChild(n.clone());
        }

        // Script-Fix: selbstschließende Skripte vermeiden
        for (Element s : templateEl.select("script")) {
            if (s.childNodeSize() == 0) s.append(""); // sorgt für <script></script>
        }

        return templateEl.outerHtml();
    }
}
