package one.xis.jsc;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import one.xis.context.XISComponent;
import one.xis.path.PathUtils;
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

    public static final String CUSTOM_SCRIPT = "custom-script.js";
    private final Pages pages;
    private final Widgets widgets;
    private final ResourceFiles resourceFiles;

    @Getter
    private String content;

    @SneakyThrows
    void createContent() {
        Document document = getRootPageDocument();
        Element head = getHead(document);
        addFunctionsScriptTag(head);
        addClasses1ScriptTag(head);
        addClasses2ScriptTag(head);
        addClasses3ScriptTag(head);
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
        pages.getKeys().forEach(key -> addScriptTag(head, PathUtils.appendPath("/xis/page/", key)));
    }

    private void addWidgetScriptTags(Element head) {
        widgets.getKeys().forEach(key -> addScriptTag(head, "/xis/widget/" + key));
    }

    private void addScriptTag(Element head, String src) {
        head.appendChild(createScriptTag(head.getOwnerDocument(), src));
    }

    private void addFunctionsScriptTag(Element head) {
        addScriptTag(head, "/xis/api/functions.js");
    }

    private void addClasses1ScriptTag(Element head) {
        addScriptTag(head, "/xis/api/classes1.js");
    }

    private void addClasses2ScriptTag(Element head) {
        addScriptTag(head, "/xis/api/classes2.js");
    }

    private void addClasses3ScriptTag(Element head) {
        addScriptTag(head, "/xis/api/classes3.js");
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
