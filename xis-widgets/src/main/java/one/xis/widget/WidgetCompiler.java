package one.xis.widget;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.js.JSScript;
import one.xis.js.JSWriter;
import one.xis.template.TemplateParser;
import one.xis.template.TemplateSynthaxException;
import one.xis.template.WidgetTemplateModel;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

@XISComponent
@RequiredArgsConstructor
class WidgetCompiler {

    private final TemplateParser templateParser;

    void compile(@NonNull Widget widget) {
        Document templateXml = htmlToDocument(widget.getClassName(), widget.getTemplateHtml());
        WidgetTemplateModel templateModel = documentToTemplateModel(widget.getClassName(), templateXml);
        JSScript script = templateModelToScriptModel(templateModel);
        widget.setJavascript(toScriptSource(script));
    }

    private WidgetTemplateModel documentToTemplateModel(String widgetClassName, Document document) {
        return templateParser.parseWidgetTemplate(document, widgetClassName);
    }

    private Document htmlToDocument(String widgetClassName, String htmlSource) {
        try {
            return htmlToDocument(htmlSource);
        } catch (SAXException e) {
            throw new TemplateSynthaxException(widgetClassName + ": " + e.getMessage());
        }
    }

    private Document htmlToDocument(String htmlSource) throws SAXException {
        try {
            return XmlUtil.loadDocument(htmlSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private JSScript templateModelToScriptModel(WidgetTemplateModel templateModel) {
        return new JSScript();
    }

    private String toScriptSource(JSScript script) {
        StringBuilder builder = new StringBuilder();
        JSWriter jsWriter = new JSWriter(builder);
        jsWriter.write(script);
        return builder.toString();
    }
}
