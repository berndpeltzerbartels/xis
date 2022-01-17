package one.xis.template;

import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Set;

class TemplateParserTest {

    private TemplateParser parser = new TemplateParser();

    @Test
    void parse() throws IOException, SAXException, TemplateSynthaxException {
        Document document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template1.html"));

        TemplateModel model = parser.parse(document, Set.of("items"));

    }
}