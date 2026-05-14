package one.xis.test.dom;

import one.xis.html.HtmlParser;

public final class ElementBuilder {
    private ElementBuilder() {
    }

    public static ElementImpl build(String html) {
        var htmlDocument = new HtmlParser().parse(html);
        return (ElementImpl) ElementMapper.map(htmlDocument.getDocumentElement());
    }


}
