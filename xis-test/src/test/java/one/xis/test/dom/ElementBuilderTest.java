package one.xis.test.dom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ElementBuilder.build")
class ElementBuilderTest {

    @Test
    @DisplayName("Fragment: genau ein Root-Element mit Unterelementen & Attributen")
    void singleElement_withChildren_andAttributes() {
        String html = """
                <section id="s1" data-x="1">
                  <h1 class="title x">Hello</h1>
                  <p><span>inner</span></p>
                </section>
                """;

        ElementImpl root = ElementBuilder.build(html); // jetzt: Root = <section>
        assertThat(root).isNotNull();
        assertThat(root.getTagName()).isEqualTo("SECTION");
        assertThat(root.getAttribute("id")).isEqualTo("s1");
        assertThat(root.getAttribute("data-x")).isEqualTo("1");
        assertThat(root.hasAttribute("data-x")).isTrue();

        // Kinder prüfen
        List<Element> children = root.getChildElements();
        assertThat(children).extracting(Element::getTagName).containsExactly("H1", "P");

        Element h1 = children.get(0);
        assertThat(h1.getCssClasses()).containsExactlyInAnyOrder("title", "x");
        assertThat(h1.getInnerText()).isEqualTo("Hello");

        Element p = children.get(1);
        assertThat(p.getChildElements()).hasSize(1);
        assertThat(p.getChildElements().get(0).getTagName()).isEqualTo("SPAN");
        assertThat(p.getChildElements().get(0).getInnerText()).isEqualTo("inner");

        // Selector-Sanity (nutzt deine querySelector-Implementierung)
        Element viaSelector = root.querySelector("section > h1.title");
        assertThat(viaSelector).isNotNull();
        assertThat(viaSelector.getTagName()).isEqualTo("H1");
        assertThat(viaSelector.getInnerText()).isEqualTo("Hello");

        // Konsistenz: firstChild / nextSibling-Kette == childNodes.list()
        var nodeList = root.getChildNodes().list();
        assertThat(nodeList).hasSize(2);
        assertThat(root.getFirstChild()).isSameAs(nodeList.get(0));
        assertThat(unwindSiblings((NodeImpl) root.getFirstChild()))
                .containsExactlyElementsOf(nodeList);
        assertThat(((NodeImpl) nodeList.get(nodeList.size() - 1)).getNextSibling()).isNull();
    }

    @Test
    @DisplayName("Fragment mit mehreren Top-Level-Knoten -> IllegalArgumentException")
    void multipleTopLevelElements_shouldThrow() {
        String html = "<h1>Hi</h1><p>Para</p>";
        assertThatThrownBy(() -> ElementBuilder.build(html))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genau EIN Top-Level-Element");
    }

    @Test
    @DisplayName("Fragment mit Element + nicht-blankem Text auf Top-Level -> IllegalArgumentException")
    void topLevelTextAndElement_shouldThrow() {
        String html = "  text <b>bold</b> ";
        assertThatThrownBy(() -> ElementBuilder.build(html))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("genau EIN Top-Level-Element");
    }

    @Test
    @DisplayName("Vollständiges Dokument: Root ist <html>, Selektoren auf head/body funktionieren")
    void fullDocument_rootIsHtml_andSelectorsWork() {
        String html = """
                <!DOCTYPE html>
                <html>
                  <head>
                    <title>t</title>
                    <script src="/bundle.min.js"></script>
                  </head>
                  <body onload="main()">
                    <div id="messages"></div>
                  </body>
                </html>
                """;

        ElementImpl htmlRoot = ElementBuilder.build(html);
        assertThat(htmlRoot.getTagName()).isEqualTo("HTML");

        Element head = htmlRoot.getElementByTagName("head");
        Element body = htmlRoot.getElementByTagName("body");
        assertThat(head).isNotNull();
        assertThat(body).isNotNull();
        assertThat(body.getAttribute("onload")).isEqualTo("main()");

        // Selektoren, die zuvor mit parseBodyFragment scheiterten:
        assertThat(htmlRoot.querySelector("head > script")).isNotNull();
        assertThat(htmlRoot.querySelector("body > div#messages")).isNotNull();

        // Konsistenz auch hier
        var bodyNodes = ((ElementImpl) body).getChildNodes().list();
        assertThat(((ElementImpl) body).getFirstChild()).isSameAs(bodyNodes.get(0));
        assertThat(unwindSiblings((NodeImpl) ((ElementImpl) body).getFirstChild()))
                .containsExactlyElementsOf(bodyNodes);
    }

    @Test
    @DisplayName("Konsistenz bei mehreren Kindern: firstChild/nextSibling-Kette entspricht childNodes")
    void siblingChain_matchesChildNodes() {
        String html = """
                <ul id="l">
                  <li>A</li>
                  <li>B</li>
                  <li>C</li>
                </ul>
                """;

        ElementImpl ul = ElementBuilder.build(html);
        assertThat(ul.getTagName()).isEqualTo("UL");
        List<Node> nodes = ul.getChildNodes().list();
        assertThat(nodes).hasSize(3);

        // firstChild
        assertThat(ul.getFirstChild()).isSameAs(nodes.get(0));

        // nextSibling-Kette == NodeList
        assertThat(unwindSiblings((NodeImpl) ul.getFirstChild()))
                .containsExactlyElementsOf(nodes);

        // letzte hat kein nextSibling
        assertThat(((NodeImpl) nodes.get(2)).getNextSibling()).isNull();

        // Elemente sind LI in Reihenfolge
        assertThat(ul.getChildElements()).extracting(Element::getTagName)
                .containsExactly("LI", "LI", "LI");
        assertThat(ul.getChildElements()).extracting(Element::getInnerText)
                .containsExactly("A", "B", "C");
    }


    @Test
    void testParameterTags() {
        final String html = """
                <xis:template>
                  <a>
                    <xis:parameter name="id" value="101"/>
                    <xis:parameter name="value" value="bla"/>
                  </a>
                </xis:template>
                """;

        ElementImpl root = ElementBuilder.build(html);
        assertThat(root).isNotNull();
        assertThat(root.getTagName()).isEqualTo("XIS:TEMPLATE");

        // <xis:template> hat genau 1 Kindelement: <a>
        List<Element> templateChildren = root.getChildElements();
        assertThat(templateChildren).hasSize(1);
        Element a = templateChildren.get(0);
        assertThat(a.getTagName()).isEqualTo("A");

        // <a> hat genau 2 Kindelemente: zwei <xis:parameter/> als SIBLINGS
        List<Element> params = a.getChildElements();
        assertThat(params).hasSize(2);
        assertThat(params).extracting(Element::getTagName)
                .containsExactly("XIS:PARAMETER", "XIS:PARAMETER");

        Element p1 = params.get(0);
        Element p2 = params.get(1);

        // Attribute prüfen
        assertThat(p1.getAttribute("name")).isEqualTo("id");
        assertThat(p1.getAttribute("value")).isEqualTo("101");
        assertThat(p2.getAttribute("name")).isEqualTo("value");
        assertThat(p2.getAttribute("value")).isEqualTo("bla");

        // Parameter sind leer (keine Kinder)
        assertThat(p1.getChildNodes().isEmpty()).isTrue();
        assertThat(p2.getChildNodes().isEmpty()).isTrue();

        // --- Konsistenz: firstChild/nextSibling-Kette == childNodes.list() ---

        // Für <a>
        var aNodeList = a.getChildNodes().list();
        assertThat(aNodeList).hasSize(2);

        // firstChild ist das erste in der Liste
        assertThat(((ElementImpl) a).getFirstChild()).isSameAs(aNodeList.get(0));

        // nextSibling-Kette entspricht exakt der NodeList-Reihenfolge
        assertThat(unwindSiblings((NodeImpl) ((ElementImpl) a).getFirstChild()))
                .containsExactlyElementsOf(aNodeList);

        // letztes Kind hat kein nextSibling
        assertThat(((NodeImpl) aNodeList.get(1)).getNextSibling()).isNull();

        // parent-Beziehung stimmt
        assertThat(((NodeImpl) p1).getParentElement()).isSameAs(a);
        assertThat(((NodeImpl) p2).getParentElement()).isSameAs(a);
    }
    // --- Helper ------------------------------------------------------------------

    /**
     * Sammle die nextSibling-Kette beginnend bei first in eine Liste.
     */
    private static List<Node> unwindSiblings(NodeImpl first) {
        List<Node> out = new ArrayList<>();
        NodeImpl cur = first;
        while (cur != null) {
            out.add(cur);
            cur = cur.getNextSibling();
        }
        return out;
    }
}
