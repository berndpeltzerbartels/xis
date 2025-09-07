package one.xis.test.dom;

import org.jsoup.Jsoup;
import org.jsoup.parser.Parser;

class DocumentBuilder {

    static Document build(String html) {
        // HTML5-tolerantes Parsing
        org.jsoup.nodes.Document js = Jsoup.parse(html, "", Parser.htmlParser());
        js.outputSettings(new org.jsoup.nodes.Document.OutputSettings()
                .prettyPrint(false)
                .syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.html));

        // Root bestimmen: bevorzugt <html>, sonst erstes Top-Level-Element, sonst <div>
        org.jsoup.nodes.Element rootJs = js.selectFirst("html");
        if (rootJs == null) {
            rootJs = js.children().isEmpty()
                    ? js.createElement("div")
                    : js.child(0);
        }

        // 1x konvertieren, fertig
        ElementImpl root = ElementBuilder.fromJsoupElement(rootJs);
        return new DocumentImpl(root);
    }
}
