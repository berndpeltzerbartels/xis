package one.xis.test.dom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ElementTest {

    private DocumentImpl document;

    @BeforeEach
    void setUp() {
        document = (DocumentImpl) Document.of("<html><body><div id=\"main\" class=\"foo bar\"><span id=\"sp1\">Text</span></div></body></html>");
    }

    @Test
    void getAttributeNamesAndGetAttribute() {
        var div = document.getElementById("main");
        assertThat(div.getAttributeNames()).contains("id", "class");
        assertThat(div.getAttribute("id")).isEqualTo("main");
        assertThat(div.getAttribute("class")).isEqualTo("foo bar");
    }

    @Test
    void setAttributeShouldChangeValue() {
        var div = document.getElementById("main");
        div.setAttribute("data-test", "123");
        assertThat(div.getAttribute("data-test")).isEqualTo("123");
    }

    @Test
    void querySelectorShouldFindElement() {
        var div = document.getDocumentElement().querySelector("div.foo");
        assertThat(div).isNotNull();
        assertThat(div.getAttribute("id")).isEqualTo("main");
    }

    @Test
    void querySelectorAllShouldFindAllMatchingElements() {
        var divs = document.getDocumentElement().querySelectorAll("div");
        assertThat(divs).hasSize(1);
        assertThat(divs.get(0).getAttribute("id")).isEqualTo("main");
    }

    @Test
    void removeChildShouldRemoveElement() {
        var div = document.getElementById("main");
        var span = div.getElementById("sp1");
        div.removeChild(span);
        assertThat(div.getChildNodes().length).isEqualTo(0);
    }

    @Test
    void insertBeforeShouldInsertElement() {
        var div = document.getElementById("main");
        var span = div.getElementById("sp1");
        var p = document.createElement("p");
        div.insertBefore(p, span);
        assertThat(div.getFirstChild()).isEqualTo(p);
        assertThat(p.getNextSibling()).isEqualTo(span);
    }

    @Test
    void getElementByIdShouldReturnSelfOrDescendant() {
        var div = document.getElementById("main");
        assertThat(div.getElementById("main")).isEqualTo(div);
        assertThat(div.getElementById("sp1").getLocalName()).isEqualTo("span");
    }

    @Test
    void getElementsByTagNameShouldReturnNodeList() {
        var div = document.getElementById("main");
        var spans = div.getElementsByTagName("span");
        assertThat(spans.length).isEqualTo(1);
        assertThat(((Element) spans.item(0)).getAttribute("id")).isEqualTo("sp1");
    }

    @Test
    void getElementsByClassShouldReturnElementsWithClass() {
        var div = document.getElementById("main");
        var elements = div.getElementsByClass("foo");
        assertThat(elements).contains(div);
    }

    @Test
    void getChildElementsShouldReturnOnlyElements() {
        var div = document.getElementById("main");
        List<Element> children = div.getChildElements();
        assertThat(children).hasSize(1);
        assertThat(children.get(0).getLocalName()).isEqualTo("span");
    }

    @Test
    void getCssClassesShouldReturnAllClasses() {
        var div = document.getElementById("main");
        assertThat(div.getCssClasses()).contains("foo", "bar");
    }

    @Test
    void getInnerTextShouldReturnTextContent() {
        var div = document.getElementById("main");
        assertThat(div.getInnerText()).isEqualTo("Text");
    }

    @Test
    void getLocalNameShouldReturnTagName() {
        var div = document.getElementById("main");
        assertThat(div.getLocalName()).isEqualTo("div");
    }

    @Test
    void findDescendantsShouldReturnMatchingNodes() {
        var div = document.getElementById("main");
        var result = div.findDescendants(node -> node instanceof Element && ((Element) node).getLocalName().equals("span"));
        assertThat(result).hasSize(1);
        assertThat(((Element) result.get(0)).getLocalName()).isEqualTo("span");
    }

    @Test
    void removeChildShouldRemoveElementAndUpdateChildNodes() {
        var div = document.getElementById("main");
        var span = div.getElementById("sp1");
        div.removeChild(span);
        var childNodes = div.getChildNodes();
        assertThat(childNodes.length).isEqualTo(0);
        assertThat(childNodes.isEmpty()).isTrue();
    }

    @Test
    void insertBeforeShouldInsertElementAndUpdateChildNodes() {
        var div = document.getElementById("main");
        var span = div.getElementById("sp1");
        var p = document.createElement("p");
        div.insertBefore(p, span);
        var childNodes = div.getChildNodes();
        assertThat(childNodes.length).isEqualTo(2);
        assertThat(childNodes.item(0)).isEqualTo(p);
        assertThat(childNodes.item(1)).isEqualTo(span);
    }

    @Test
    void appendChildShouldUpdateChildNodes() {
        var div = document.getElementById("main");
        var p = document.createElement("p");
        div.appendChild(p);
        var childNodes = div.getChildNodes();
        assertThat(childNodes.length).isEqualTo(2);
        assertThat(childNodes.item(1)).isEqualTo(p);
    }
}