package one.xis.widget;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.template.TemplateParser;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

@XISComponent
@RequiredArgsConstructor
class WidgetCompiler {

    private final TemplateParser templateParser;

    String compile(String htmlTemplate) {
        //Document templateXml = htmlToDocument(htmlTemplate);
        //JSScript script = new JSScript();
        return null;
    }


    private Document htmlToDocument(String htmlSource) throws IOException, SAXException {
        return XmlUtil.loadDocument(htmlSource);
    }
}
