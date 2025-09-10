package one.xis.html.document;

import lombok.RequiredArgsConstructor;
import one.xis.html.parts.Part;
import one.xis.html.tokens.HtmlParseException;

import java.util.List;

@RequiredArgsConstructor
public class DocumentBuilder {
    private final List<Part> parts;
    private final HtmlDocument document = new HtmlDocument();

    public HtmlDocument build() {
        if (parts.isEmpty()) {
            throw new HtmlParseException("No parts to build document from");
        }
        if (parts.get(0) instanceof Doctype doctype) {
            document.setDoctype(doctype);
        }

        document.setDocumentElement(new ElementBuilder(parts).build());
        return document;
    }
}
