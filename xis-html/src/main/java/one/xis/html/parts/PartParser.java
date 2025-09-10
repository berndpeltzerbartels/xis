package one.xis.html.parts;

import lombok.RequiredArgsConstructor;
import one.xis.html.document.SelfClosingTags;
import one.xis.html.tokens.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Token-to-Part parser with early validation and small focused methods.
 * <p>
 * Guarantees:
 * - Does not throw just because the document doesn't start with a DOCTYPE.
 * - Emits one Part per XML-style self-closing tag (&lt;ns:tag/&gt;).
 * - Forbids closing tags for HTML5 void elements (e.g. &lt;/br&gt;).
 * - Detects duplicate attribute names and basic attribute syntax errors.
 * <p>
 * All comments and error messages are in English.
 */
@RequiredArgsConstructor
public class PartParser {

    private final List<Token> tokens;
    private final List<Part> parts = new ArrayList<>();
    private int index = 0;

    /* --------------------------- Public API --------------------------- */

    public List<Part> parse() {
        parts.clear();

        DoctypePart doctype = tryReadDoctype();
        if (doctype != null) {
            parts.add(doctype);
        }

        while (index < tokens.size()) {
            readOne();
        }
        return parts;
    }

    /* --------------------------- Main loop --------------------------- */

    private void readOne() {
        if (index >= tokens.size()) return;

        Token token = currentToken();

        if (token instanceof TextToken text) {
            parts.add(new TextPart(text.getText()));
            index++;
            return;
        }

        if (token instanceof OpenBracketToken) {
            parts.add(readTag());
            return;
        }

        throw error("Unexpected token: " + tokenToString(token) + " at position " + index);
    }

    /* --------------------------- Tag parsing --------------------------- */

    private Tag readTag() {
        skip(OpenBracketToken.class);

        if (is(SlashToken.class)) {
            return readClosingTag();
        }
        return readOpeningOrSelfClosingTag();
    }

    private Tag readClosingTag() {
        skip(SlashToken.class);
        TextToken nameTok = skip(TextToken.class);
        String name = nameTok.getText();

        // Early validation: HTML5 void elements must not have a closing form.
        if (SelfClosingTags.isSelfClosing(name)) {
            throw error("Void element </" + name + "> is not allowed. Void elements must not have end tags.");
        }

        Tag tag = new Tag(name);
        tag.setTagType(TagType.CLOSING);

        skip(CloseBracketToken.class);
        return tag;
    }

    private Tag readOpeningOrSelfClosingTag() {
        TextToken nameTok = skip(TextToken.class);
        String name = nameTok.getText();

        Tag tag = new Tag(name);

        if (is(TextToken.class)) {
            tag.setAttributes(readAttributes());
        }

        if (is(SlashToken.class)) {
            // <name .../>
            skip(SlashToken.class);
            skip(CloseBracketToken.class);
            tag.setTagType(TagType.NO_CONTENT);
            return tag;
        }

        if (is(CloseBracketToken.class)) {
            // <name ...>
            skip(CloseBracketToken.class);
            tag.setTagType(TagType.OPENING);
            return tag;
        }

        throw error("Unexpected token in tag: " + tokenToString(currentToken()) + " at position " + index
                + ". Expected '/', '>' or attribute name.");
    }

    /* --------------------------- DOCTYPE --------------------------- */

    /**
     * Attempts to read a leading DOCTYPE. If not present, returns null and does not throw.
     */
    private DoctypePart tryReadDoctype() {
        int save = index;
        if (index >= tokens.size()) return null;
        if (!(currentToken() instanceof OpenBracketToken)) return null;

        skip(OpenBracketToken.class);

        if (!(currentToken() instanceof TextToken tt) || !"!DOCTYPE".equalsIgnoreCase(tt.getText())) {
            index = save;
            return null;
        }
        skip(TextToken.class); // "!DOCTYPE"

        if (!(currentToken() instanceof TextToken nameTok)) {
            // Be lenient: restore and ignore malformed doctype
            index = save;
            return null;
        }
        String name = nameTok.getText();
        skip(TextToken.class);

        // Be lenient: consume up to the next '>'
        while (index < tokens.size() && !(currentToken() instanceof CloseBracketToken)) {
            index++;
        }
        if (index < tokens.size()) skip(CloseBracketToken.class);

        return new DoctypePart(name);
    }

    /* --------------------------- Attributes --------------------------- */

    private Map<String, String> readAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        while (is(TextToken.class)) {
            readAttributePairInto(attrs);
        }
        return attrs;
    }

    private void readAttributePairInto(Map<String, String> attrs) {
        TextToken nameTok = skip(TextToken.class);
        String attrName = nameTok.getText();

        if (attrs.containsKey(attrName)) {
            throw error("Duplicate attribute '" + attrName + "' at position " + (index - 1));
        }

        if (!is(EqualsToken.class)) {
            throw error("Expected '=' after attribute name '" + attrName + "' at position " + index);
        }
        skip(EqualsToken.class);

        if (!(currentToken() instanceof TextToken valueTok)) {
            throw error("Expected attribute value for '" + attrName + "' at position " + index);
        }
        skip(TextToken.class);

        attrs.put(attrName, valueTok.getText());
    }

    /* --------------------------- Token helpers --------------------------- */

    private <T extends Token> T skip(Class<T> expected) {
        ensureNotEof(expected.getSimpleName());
        Token tok = tokens.get(index++);
        if (!expected.isInstance(tok)) {
            throw error("Expected " + expected.getSimpleName() + " at position " + (index - 1)
                    + " but found " + tokenToString(tok) + ".");
        }
        return expected.cast(tok);
    }

    private boolean is(Class<? extends Token> type) {
        return index < tokens.size() && type.isInstance(tokens.get(index));
    }

    private Token currentToken() {
        return tokens.get(index);
    }

    private void ensureNotEof(String expected) {
        if (index >= tokens.size()) {
            throw error("Unexpected end of input at position " + index + " (expected " + expected + ").");
        }
    }

    private HtmlParseException error(String message) {
        return new HtmlParseException(message);
    }

    private static String tokenToString(Token t) {
        if (t == null) return "null";
        return t.getClass().getSimpleName() + (t instanceof TextToken tt ? "('" + tt.getText() + "')" : "");
    }
}
