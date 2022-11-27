package one.xis.root;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.context.XISComponent;
import one.xis.page.PageService;
import one.xis.path.PathUtils;
import one.xis.resource.ResourceFile;
import one.xis.resource.ResourceFiles;
import one.xis.utils.xml.XmlUtil;
import one.xis.widget.WidgetService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;

@XISComponent
@RequiredArgsConstructor
public class RootPage {

    public static final String CUSTOM_SCRIPT = "custom-script.js";
    private final PageService pageService;
    private final WidgetService widgetJavascripts;
    private final ResourceFiles resourceFiles;

    @Getter
    private String content;

    @SneakyThrows
    void createContent() {
        Document document = getRootPageDocument();
        Element head = getHead(document);
        addFunctionsScriptTag(head);
        addApiScriptTag(head);
        addGlobalsScriptTag(head);
        addPagesScriptTags(head);
        addWidgetScriptTags(head);
        addInitializerScriptTag(head);
        addCustomScriptTag(head);
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
        pageService.getClassnames().forEach(jsClassname -> addScriptTag(head, PathUtils.appendPath("/xis/page/", jsClassname) + ".js"));
    }

    private void addWidgetScriptTags(Element head) {
        widgetJavascripts.getClassnames().forEach(jsClassname -> addScriptTag(head, PathUtils.appendPath("/xis/widget/", jsClassname) + ".js"));
    }

    private void addScriptTag(Element head, String src) {
        head.appendChild(createScriptTag(head.getOwnerDocument(), src));
    }

    private void addFunctionsScriptTag(Element head) {
        addScriptTag(head, "/xis/api/functions.js");
    }

    private void addApiScriptTag(Element head) {
        addScriptTag(head, "/xis/api/xis.js");
    }

    private void addInitializerScriptTag(Element head) {
        addScriptTag(head, "/xis/initializer.js");
    }

    private void addGlobalsScriptTag(Element head) {
        addScriptTag(head, "/xis/api/xis-globals.js");
    }

    private void addCustomScriptTag(Element head) {
        addScriptTag(head, "/xis/api/custom-script.js");
    }

    private Element createScriptTag(Document document, String src) {
        Element element = document.createElement("script");
        element.setAttribute("type", "text/javascript");
        element.setAttribute("src", src);
        return element;
    }
}
