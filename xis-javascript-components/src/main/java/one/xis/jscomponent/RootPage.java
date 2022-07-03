package one.xis.jscomponent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.context.XISComponent;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

@XISComponent
@RequiredArgsConstructor
class RootPage {

    private final Pages pages;
    private final Widgets widgets;
    private final ResourceFiles resourceFiles;

    @Getter
    private String content;

    @SneakyThrows
    void createContent() {
        Document document = getRootPageDocument();
        Element head = getHead(document);
        addPagesScriptTags(head);
        addWidgetScriptTags(head);
        content = XmlUtil.asString(document);
    }

    private Element getHead(Document rootPageDocument) {
        return XmlUtil.getElementByTagName(rootPageDocument.getDocumentElement(), "head").orElseThrow();
    }

    private Document getRootPageDocument() throws IOException, SAXException {
        ResourceFile rootPageResource = resourceFiles.getByPath("main.html");
        return XmlUtil.loadDocument(rootPageResource.getContent());
    }

    private void addPagesScriptTags(Element head) {
        pages.getNames().stream().map(JavasscriptComponentUtils::nameToUrn).forEach(urn -> addScriptTag(head, "/xis/page/" + urn));
    }

    private void addWidgetScriptTags(Element head) {
        widgets.getNames().stream().map(JavasscriptComponentUtils::nameToUrn).forEach(urn -> addScriptTag(head, "/xis/widget/" + urn));
    }

    private void addScriptTag(Element head, String src) {
        head.appendChild(createScriptTag(head.getOwnerDocument(), src));
    }

    private Element createScriptTag(Document document, String src) {
        Element element = document.createElement("script");
        element.setAttribute("type", "text/javascript");
        element.setAttribute("src", src);
        return element;
    }
}
