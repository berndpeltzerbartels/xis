package one.xis.template;

import one.xis.template.TemplateModel.ForElement;
import one.xis.template.TemplateModel.IfElement;
import one.xis.template.TemplateModel.XmlElement;
import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Set;

import static one.xis.template.TemplateTestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

class TemplateParserTest {

    private TemplateParser parser = new TemplateParser();

    @Test
    void parseTemplate1() throws IOException, SAXException, TemplateSynthaxException {
        Document document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template1.html"));

        TemplateModel model = parser.parse(document, Set.of("items"));

        IfElement ifElement = assertRootIsIf(model, "notEmpty(items)");
        XmlElement ul = assertContainsXml(ifElement, "ul");
        ForElement forElement = assertContainsFor(ul, "items", "item", "i");
        assertContainsXml(forElement, "li");

        assertThat(ul.getAttributes()).isEmpty();// leave data-if  data-for etc.

    }

    @Test
    void parseTemplate2() throws IOException, SAXException, TemplateSynthaxException {
        Document document = XmlUtil.loadDocument(IOUtils.getResourceForClass(getClass(), "Template2.html"));

        TemplateModel model = parser.parse(document, Set.of("items"));
        

    }

}