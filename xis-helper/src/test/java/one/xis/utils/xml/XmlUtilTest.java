package one.xis.utils.xml;

import one.xis.utils.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class XmlUtilTest {

    private Element ul;

    @BeforeEach
    void setUp() throws IOException, SAXException {
        Document document = new XmlLoader().loadDocument(IOUtils.getResourceAsStream("FragmentLoopExpression.html"));
        ul = document.getDocumentElement();
    }

    @Test
    void getElementByTagName() {
        Element li = XmlUtil.getElementByTagName(ul, "li").orElseThrow();
        AssertionsForClassTypes.assertThat(li.getTagName()).isEqualTo("li");
    }

    @Test
    void getElementsByTagName() {
        Collection<Element> elements = XmlUtil.getElementsByTagName(ul, "li").collect(Collectors.toSet());

        AssertionsForClassTypes.assertThat(elements.size()).isEqualTo(1);
    }
}