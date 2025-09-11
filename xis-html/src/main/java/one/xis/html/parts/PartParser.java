package one.xis.html.parts;

import lombok.RequiredArgsConstructor;
import one.xis.html.document.SelfClosingTags;
import one.xis.html.tokens.*;

import java.util.*;

/**
 * Token-to-Part parser with early validation and small focused methods.
 * <p>
 * Guarantees:
 * - Does not throw just because the document doesn't start with a DOCTYPE.
 * - Emits one Part per XML-style self-closing tag (<ns:tag/>).
 * - Forbids closing tags for HTML5 void elements (e.g. </br>).
 * - Detects duplicate attribute names and basic attribute syntax errors.
 * - Allows HTML boolean attributes (attribute without a value) on non-namespaced tags.
 * For namespaced/XML-style tags (e.g. <xis:...>), attributes must have values.
 * <p>
 * All comments and error messages are in English.
 */
@RequiredArgsConstructor
public class PartParser {

    private final List<Token> tokens;
    private final List<Part> parts = new ArrayList<>();
    private int index = 0;

    /**
     * Allow-list of HTML boolean attributes (presence implies true / empty string value).
     */
    private static final Set<String> HTML_BOOLEAN_ATTRS = Set.of(
            "disabled", "checked", "required", "readonly", "autofocus", "multiple",
            "novalidate", "selected", "hidden", "loop", "muted", "controls", "open",
            "reversed", "itemscope", "async", "defer", "playsinline", "allowfullscreen",
            "autoplay", "formnovalidate", "nomodule"
    );

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

        if (token instanceof TextToken textToken) {
            // Skip whitespace-only text tokens at this stage
            if (!textToken.getText().trim().isEmpty()) {
                parts.add(new TextPart(textToken.getText()));
            }
            index++;
            return;
        }

        if (token instanceof OpenBracketToken) {
            // NEW: Skip HTML comments <!-- ... -->
            if (isCommentStart()) {
                skipComment();
                return; // do not emit a Part for comments
            }
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

        // Read attributes (owner-aware for boolean-attr rules)
        if (is(TextToken.class)) {
            tag.setAttributes(readAttributes(name));
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

    /**
     * Reads attributes for a given owner tag.
     * - For namespaced (XML-style) tags (owner contains ':'), every attribute must have a value.
     * - For plain HTML tags, boolean/minimized attributes are allowed for a known allow-list.
     */
    private Map<String, String> readAttributes(String ownerTagName) {
        Map<String, String> attrs = new LinkedHashMap<>();
        boolean xmlStyle = ownerTagName.contains(":"); // treat namespaced tags as XML-style

        while (is(TextToken.class)) {
            readAttributePairInto(attrs, xmlStyle);
        }
        return attrs;
    }

    private void readAttributePairInto(Map<String, String> attrs, boolean xmlStyle) {
        TextToken nameTok = skip(TextToken.class);
        String attrName = nameTok.getText();
        String attrNameLc = attrName.toLowerCase(Locale.ROOT);

        if (attrs.containsKey(attrName)) {
            throw error("Duplicate attribute '" + attrName + "' at position " + (index - 1));
        }

        // Case 1: name=value
        if (is(EqualsToken.class)) {
            skip(EqualsToken.class);

            if (!(currentToken() instanceof TextToken valueTok)) {
                throw error("Expected attribute value for '" + attrName + "' at position " + index);
            }
            skip(TextToken.class);
            attrs.put(attrName, valueTok.getText());
            return;
        }

        // Case 2: bare attribute (boolean/minimized)
        if (xmlStyle) {
            // XML-style tags must always have values
            throw error("Attribute '" + attrName + "' requires a value in XML-style tags.");
        }

        if (HTML_BOOLEAN_ATTRS.contains(attrNameLc)) {
            // Represent boolean attribute presence; choose "" or "true" consistently.
            attrs.put(attrName, "true");
        } else {
            // Be strict for plain HTML to catch typos; relax here if you want leniency.
            throw error("Attribute '" + attrName + "' requires a value (not a known HTML boolean attribute).");
        }
    }

    /* --------------------------- Comment handling --------------------------- */

    /**
     * Detects a comment start sequence: "<" followed by a TextToken starting with "!--".
     */
    private boolean isCommentStart() {
        int next = index + 1;
        if (index >= tokens.size() || !(tokens.get(index) instanceof OpenBracketToken)) return false;
        if (next >= tokens.size()) return false;
        Token t = tokens.get(next);
        return (t instanceof TextToken tt) && tt.getText().startsWith("!--");
    }

    /**
     * Consumes a comment: '<' '!--...' ... '--' '>' (lenient if malformed). Emits no Part.
     */
    private void skipComment() {
        // consume '<'
        skip(OpenBracketToken.class);

        // consume a TextToken that starts with "!--" (could be "!--", "!--foo", etc.)
        if (currentToken() instanceof TextToken tt && tt.getText().startsWith("!--")) {
            skip(TextToken.class);
        } else {
            // Not actually a comment; be defensive and just return
            return;
        }

        // scan until a TextToken containing "--" appears
        while (index < tokens.size()) {
            Token t = currentToken();

            if (t instanceof TextToken txt) {
                String s = txt.getText();
                if (s.contains("--")) {
                    // consume the token that contains "--"
                    skip(TextToken.class);

                    // now consume tokens up to the next '>' (CloseBracketToken), if any
                    while (index < tokens.size() && !(currentToken() instanceof CloseBracketToken)) {
                        index++;
                    }
                    if (index < tokens.size() && currentToken() instanceof CloseBracketToken) {
                        skip(CloseBracketToken.class);
                    }
                    return; // comment fully skipped
                } else {
                    // not the end yet; skip this token
                    index++;
                    continue;
                }
            }

            // If we hit '>' before seeing "--", treat as malformed but finish gracefully
            if (t instanceof CloseBracketToken) {
                skip(CloseBracketToken.class);
                return;
            }

            // Any other token inside the comment: skip
            index++;
        }
        // EOF reached: be lenient and just return
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
