package one.xis.test.dom;

import one.xis.html.HtmlParser;

class DocumentBuilder {

    static Document build(String html) {
        var htmlDocument = new HtmlParser().parse(html);
        return DocumentMapper.map(htmlDocument);
    }

}
