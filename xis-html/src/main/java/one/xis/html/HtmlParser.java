package one.xis.html;

import one.xis.html.document.Doctype;
import one.xis.html.document.HtmlDocument;

import java.util.Optional;

public class HtmlParser {

    public HtmlDocument parse(String html) {
        var document = new HtmlDocument();
        var htmlBuilder = new StringBuilder(html);
        var doctypeOpt = readDoctype(htmlBuilder);
        doctypeOpt.ifPresent(doctypeText -> document.setDoctype(new Doctype(doctypeText)));

        var element = new ElementParser().parse(htmlBuilder.toString());
        document.setDocumentElement(element);
        return document;
    }

    private Optional<String> readDoctype(StringBuilder html) {
        if (!html.toString().toUpperCase().startsWith("<!DOCTYPE")) {
            return Optional.empty();
        }
        StringBuilder doctype = new StringBuilder();
        int index = 9; // length of "<!DOCTYPE"
        while (index < html.length()) {
            char c = html.charAt(index);
            if (c == '>') {
                break;
            }
            doctype.append(c);
            index++;
        }
        html.delete(0, index + 1); // remove doctype from html
        return Optional.of(doctype.toString().trim());
    }


}

