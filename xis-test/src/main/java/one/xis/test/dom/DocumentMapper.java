package one.xis.test.dom;

import one.xis.html.document.HtmlDocument;

/**
 * Utility to map an HtmlDocument from xis-html-parser to a Document from xis-test-dom.
 * This is a simplified example and may need enhancements for full fidelity.
 */
public class DocumentMapper {

    /**
     * Maps an HtmlDocument from xis-html-parser to a Document from xis-test-dom.
     *
     * @param htmlDocument
     * @return
     */
    public static Document map(HtmlDocument htmlDocument) {
        if (htmlDocument == null || htmlDocument.getDocumentElement() == null) {
            throw new IllegalArgumentException("HtmlDocument or its documentElement is null");
        }
        var documentElement = ElementMapper.map(htmlDocument.getDocumentElement());
        return new DocumentImpl((ElementImpl) documentElement);
    }

}
