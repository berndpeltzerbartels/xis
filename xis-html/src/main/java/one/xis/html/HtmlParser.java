package one.xis.html;

import one.xis.html.document.DocumentBuilder;
import one.xis.html.document.HtmlDocument;
import one.xis.html.parts.PartParser;
import one.xis.html.tokens.HtmlParseException;
import one.xis.html.tokens.HtmlTokenizer;


public class HtmlParser {

    private final HtmlTokenizer htmlTokenizer = new HtmlTokenizer();

    public HtmlDocument parse(String html) throws HtmlParseException {
        var tokens = htmlTokenizer.tokenize(html);
        var partParser = new PartParser(tokens);
        var parts = partParser.parse();

        var builder = new DocumentBuilder(parts);
        return builder.build();
    }

}
