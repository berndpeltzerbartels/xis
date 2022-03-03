package one.xis.utils.xml;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;


class XmlLoaderTest {

    private XmlLoader xmlLoader = new XmlLoader();

    @Test
    void loadDocument() throws IOException, SAXException {
        Document document = xmlLoader.loadDocument(IOUtils.getResourceAsStream("FragmentLoopExpression.html"));

        assertThat(document).isNotNull();
        assertThat(document.getDocumentElement().getTagName()).isEqualTo("ul");
        assertThat(document.getDocumentElement().getAttribute("data-for")).isEqualTo("item:items");
    }
}