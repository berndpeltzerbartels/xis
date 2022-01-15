package one.xis.template;

import one.xis.utils.IOUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class XmlSerializerTest {

    private Element li;

    @BeforeAll
    void setup() throws IOException, SAXException {
        Document document = new XmlLoader().loadDocument(IOUtils.getResourceAsStream("FragmentLoopExpression.html"));
        li = XmlUtil.getElementByTagName(document.getDocumentElement(), "li").orElseThrow();
    }

    @Test
    void serializeElement() throws TransformerException {
        String str = new XmlSerializer().serialize(li);

        assertThat(str).doesNotContain("<?xml");
        assertThat(str).contains("<li ");
        assertThat(str).contains("class=\"list\"");
    }

}