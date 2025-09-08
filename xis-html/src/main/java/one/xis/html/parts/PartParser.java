package one.xis.html.parts;

import lombok.RequiredArgsConstructor;
import one.xis.html.tokens.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class PartParser {

    private final List<Token> tokens;
    private final List<Part> parts = new ArrayList<>();
    private int index = 0;

    public List<Part> parse() {
        parts.clear();
        var doctype = readDoctype();
        if (doctype != null) {
            parts.add(doctype);
        }
        while (index < tokens.size()) {
            read();
        }
        return parts;
    }

    private void read() {
        if (index >= tokens.size() - 1) {
            return;
        }

        Token token = currentToken();
        if (token instanceof TextToken textToken) {
            parts.add(new TextPart(textToken.getText()));
            index++;
        } else if (token instanceof OpenBracketToken) {
            parts.add(readTag());
        } else {
            throw new HtmlParseException("Unexpected token: " + token + " at position " + index);
        }
    }

    private Tag readTag() {
        skipToken(OpenBracketToken.class);
        // </name>
        if (currentToken() instanceof SlashToken) {
            skipToken(SlashToken.class);
            var nameToken = skipToken(TextToken.class);
            var tag = new Tag(nameToken.getText());
            tag.setOpenTag(false);
            skipToken(CloseBracketToken.class);
            return tag;
        }
        var nameToken = skipToken(TextToken.class);
        var tag = new Tag(nameToken.getText());
        // <name  attr="value"/>
        // read attributes
        if (currentToken() instanceof TextToken) {
            tag.setAttributes(readAttributes());
        }
        if (currentToken() instanceof SlashToken) {
            tag.setOpenTag(true);
            tag.setEmpty(true);
            skipToken(SlashToken.class);
            skipToken(CloseBracketToken.class);
            return tag;
        }
        if (currentToken() instanceof CloseBracketToken) {
            tag.setEmpty(false);
            tag.setOpenTag(true);
            skipToken(CloseBracketToken.class);
            return tag;
        }
        throw new HtmlParseException("Unexpected token in tag: " + currentToken() + " at position " + index
                + ". Expected '/', '>' or attribute name.");
    }

    private DoctypePart readDoctype() {
        if (!(currentToken() instanceof OpenBracketToken)) {
            throw new HtmlParseException("Expected '<' at position " + index);
        }
        skipToken(OpenBracketToken.class);
        if (!(currentToken() instanceof TextToken textToken) || !"!DOCTYPE".equalsIgnoreCase(textToken.getText())) {
            index = 0;
            return null;
        }
        skipToken(TextToken.class); // DOCTYPE
        // read name
        var nameToken = skipToken(TextToken.class);
        var doctype = new DoctypePart(nameToken.getText());
        skipToken(CloseBracketToken.class);
        return doctype;
    }

    private Map<String, String> readAttributes() {
        var attributes = new HashMap<String, String>();
        while (currentToken() instanceof TextToken) {
            var nameToken = skipToken(TextToken.class);
            var name = nameToken.getText();
            skipToken(EqualsToken.class);
            var valueToken = skipToken(TextToken.class);
            var value = valueToken.getText();
            attributes.put(name, value);
        }
        return attributes;
    }

    private <T extends Token> T skipToken(Class<T> expectedType) throws HtmlParseException {
        if (index >= tokens.size()) {
            throw new HtmlParseException("Unexpected end of input at position " + index);
        }
        var token = tokens.get(index++);
        if (!expectedType.isInstance(token)) {
            throw new HtmlParseException("Expected " + expectedType.getSimpleName() + " at position " + index);
        }
        return expectedType.cast(token);
    }

    private Token nextToken() {
        if (index + 1 >= tokens.size()) {
            throw new HtmlParseException("Unexpected end of input at position " + (index + 1));
        }
        return tokens.get(index + 1);
    }

    private Token currentToken() {
        return tokens.get(index);
    }

}
