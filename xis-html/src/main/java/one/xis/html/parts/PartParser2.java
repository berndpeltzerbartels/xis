package one.xis.html.parts;

import lombok.RequiredArgsConstructor;
import one.xis.html.document.ParenthesisToken;
import one.xis.html.tokens.*;

import java.util.*;

/**
 * Token-to-Part parser.
 * <p>
 * RULE:
 * Once a tag is opened, EVERYTHING is text until a '</' sequence is found.
 * Only then is a closing tag parsed and validated.
 * <p>
 * This applies to ALL tags (script, div, etc.) without exceptions.
 */
@RequiredArgsConstructor
public class PartParser2 {

    private static final Token EOF = new EOFToken();

    private final List<Token> tokens;
    private final List<Part> parts = new ArrayList<>();
    private final Deque<OpeningNode> openingNodes = new ArrayDeque<>();
    private int index = 0;

    /* --------------------------- Public API --------------------------- */

    public List<Part> parse() {
        parts.clear();
        openingNodes.clear();
        index = 0;

        DoctypePart doctype = tryReadDoctype();
        if (doctype != null) {
            parts.add(doctype);
            index += doctype.tokenCount();
        }

        while (index < tokens.size()) {
            if (isEOF()) {
                break;
            }
            if (isTagStart()) {
                var tagOpen = consumeTagOpen();
                parts.add(tagOpen);
            } else if (isClosingTag()) {
                parts.add(consumeTagClose());
            } else if (currentToken() instanceof OpenCommentToken) {
                parts.add(new CommentOpen());
                index++;
            } else if (currentToken() instanceof CloseCommentToken) {
                parts.add(new CommentClose());
                index++;
            } else {
                consumeText();

            }
        }

        if (!openingNodes.isEmpty()) {
            throw error("Unclosed tag <" + openingNodes.peek() + ">");
        }

        return parts;
    }

    private boolean isEOF() {
        return currentToken() instanceof EOFToken;
    }

    private boolean isTagStart() {
        var token1 = followingToken(0);
        var token2 = followingToken(1);
        var token3 = followingToken(2);
        if (!(token1 instanceof OpenBracketToken) || !(token2 instanceof TextToken)) {
            return false;
        }
        if (token3 instanceof CloseBracketToken) {
            return true;
        }
        if (token3 instanceof WhitespaceToken) {
            var pos2 = 3;
            while (true) {
                int nextPos = isAttribute(pos2);
                if (nextPos == -1) {
                    return false;
                }
                var nextToken = followingToken(nextPos);
                if (nextToken instanceof CloseBracketToken) {
                    return true;
                } else if (nextToken instanceof WhitespaceToken) {
                    pos2++;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    private boolean isClosingTag() {
        var token1 = followingToken(0);
        var token2 = followingToken(1);
        var token3 = followingToken(2);
        return token1 instanceof OpenBracketToken &&
                token2 instanceof SlashToken &&
                token3 instanceof TextToken;
    }

    private int isAttribute(int pos) {
        var token1 = followingToken(pos);
        var token2 = followingToken(pos + 1);
        var token3 = followingToken(pos + 2);
        if (token1 instanceof TextToken &&
                token2 instanceof EqualsToken &&
                token3 instanceof ParenthesisToken) {
            return parenthesisFollow(pos + 3);
        }
        return -1;
    }

    private int parenthesisFollow(int pos) {
        var token = followingToken(pos);
        while (!(token instanceof EOFToken)) {
            if (token instanceof ParenthesisToken) {
                return pos + 1;
            }
            token = followingToken(++pos);
        }
        return -1;

    }

    private OpeningTag consumeTagOpen() {
        consumeToken(OpenBracketToken.class);
        String name = consumeToken(TextToken.class).getText();
        var tagOpen = new OpeningTag(name);
        tagOpen.addTokenCount(2);
        tagOpen.setAttributes(consumeAttributes());
        return tagOpen;
    }

    private ClosingTag consumeTagClose() {
        consumeToken(OpenBracketToken.class);
        consumeToken(SlashToken.class);
        String name = consumeToken(TextToken.class).getText();
        consumeToken(CloseBracketToken.class);
        return new ClosingTag(name);
    }

    private Map<String, String> consumeAttributes() {
        Map<String, String> attrs = new LinkedHashMap<>();
        outer:
        while (index < tokens.size()) {
            if (currentToken() instanceof WhitespaceToken) {
                consumeToken(WhitespaceToken.class);
                continue;
            }
            if (currentToken() instanceof CloseBracketToken) {
                consumeToken(CloseBracketToken.class);
                break;
            }
            var name = consumeToken(TextToken.class).getText();
            consumeToken(EqualsToken.class);
            consumeToken(ParenthesisToken.class);
            StringBuilder value = new StringBuilder();

            while (!(currentToken() instanceof EOFToken)) {
                var token = consumeToken();
                if (token instanceof ParenthesisToken) {
                    attrs.put(name, value.toString());
                    continue outer;
                }
                value.append(token.toString());
            }
        }
        return attrs;
    }


    record Attribute(String name, String value) {
    }


    private void consumeText() {
        StringBuilder text = new StringBuilder();
        while (index < tokens.size()) {
            if (isEOF()) {
                break;
            } else if (isTagStart()) {
                if (!text.isEmpty()) {
                    parts.add(new TextPart(text.toString()));
                    text.setLength(0);
                }
                var tagOpen = consumeTagOpen();
                parts.add(tagOpen);
            } else if (isClosingTag()) {
                if (!text.isEmpty()) {
                    parts.add(new TextPart(text.toString()));
                    text.setLength(0);
                }
                parts.add(consumeTagClose());
            } else if (currentToken() instanceof OpenCommentToken) {
                if (!text.isEmpty()) {
                    parts.add(new TextPart(text.toString()));
                    text.setLength(0);
                }
                parts.add(new CommentOpen());
                index++;
            } else if (currentToken() instanceof CloseCommentToken) {
                parts.add(new CommentClose());
                index++;
            } else {
                text.append(consumeToken().toString());
            }
        }
    }

    private void readUntilClose(OpeningNode openingNode) {
        if (openingNode instanceof OpeningTag openingTag) {
            readNodeContent(openingTag);
        } else if (openingNode instanceof CommentOpen) {
            readCommentUntilClose();
        } else {
            consumeText();
        }
    }

    private void readNodeContent(OpeningTag openingTag) {
        var text = new StringBuilder();
        while (index < tokens.size()) {
            var token1 = followingToken(0);
            var token2 = followingToken(1);
            var token3 = followingToken(2);
            var token4 = followingToken(3);
            if (token1 instanceof OpenBracketToken) {
                if (token2 instanceof TextToken) {
                    OpeningTag nextOpen = readTagOpen();
                    if (nextOpen == null) {
                        // not a valid tag, treat as text
                        text.append("<");
                        index++;
                    } else {
                        // found another opening tag inside content
                        if (!text.isEmpty()) {
                            parts.add(new TextPart(text.toString()));
                            text.setLength(0);
                        }
                        parts.add(nextOpen);
                        openingNodes.addLast(nextOpen);
                        index += nextOpen.tokenCount();
                    }

                } else if (token2 instanceof SlashToken && token3 instanceof TextToken nameToken) {
                    if (nameToken.getText().equals(openingTag.getLocalName()) && token4 instanceof CloseBracketToken) {
                        if (!text.isEmpty()) {
                            parts.add(new TextPart(text.toString()));
                            text.setLength(0);
                        }
                        // found closing tag
                        parts.add(new ClosingTag(openingTag.getLocalName()));
                        index += 4; // consume '</', name, '>'
                        return;
                    } else {
                        index++;
                        // not the correct closing tag
                        text.append("<");
                    }
                }
            } else if (token1 instanceof OpenCommentToken) {
                var commentOpen = new CommentOpen();
                openingNodes.add(commentOpen);
                parts.add(commentOpen);
                index++;
                readCommentUntilClose();
            } else if (token1 instanceof CommentClose) {
                throw new HtmlParseException("Unexpected comment close -->");
            } else {
                // literal text
                text.append(token1.toString());
                index++;
            }

            index++;
        }
    }

    private OpeningTag readTagOpen() {
        consumeToken(OpenBracketToken.class);
        String name = consumeToken(TextToken.class).getText();
        var tagOpen = new OpeningTag(name);
        tagOpen.addTokenCount(2);
        return readAttributes(tagOpen) ? tagOpen : null;
    }

    private boolean readAttributes(OpeningTag openingTag) {
        Map<String, String> attrs = new LinkedHashMap<>();
        while (index < tokens.size()) {
            var token1 = followingToken(0);
            var token2 = followingToken(1);
            var token3 = followingToken(2);
            if (!(token1 instanceof TextToken)) {
                return false;
            }
            if (token2 instanceof CloseBracketToken) {
                openingTag.addTokenCount(1);
                attrs.put(consumeToken(TextToken.class).getText(), "true");
                openingTag.setAttributes(attrs);
                return true; // end of tag
            }
            if (!(token2 instanceof EqualsToken)) {
                return false;
            }
            if (!(token3 instanceof TextToken)) {
                return false;
            }
            openingTag.addTokenCount(3);
            var name = consumeToken(TextToken.class).getText();
            consumeToken(EqualsToken.class);
            String value = consumeToken(TextToken.class).getText();
            if (attrs.containsKey(name)) {
                throw error("Duplicate attribute '" + name + "'");
            }
            attrs.put(name, value);
        }
        openingTag.setAttributes(attrs);
        return true;
    }

    private <T extends Token> T consumeToken(Class<T> type) {
        if (index < tokens.size()) {
            var token = tokens.get(index++);
            if (!type.isInstance(token)) {
                throw new HtmlParseException("Expected token of type " + type.getSimpleName() + " but found " + token);
            }
            return type.cast(token);
        }
        throw new HtmlParseException("Unexpected end of input");
    }

    private Token consumeToken() {
        if (index < tokens.size()) {
            return tokens.get(index++);
        }
        throw new HtmlParseException("Unexpected end of input");
    }

    private void readCommentUntilClose() {
        consumeToken(OpenCommentToken.class);
        parts.add(new CommentOpen());
        TextPart textPart = new TextPart();
        while (index < tokens.size()) {
            Token token = tokens.get(index);
            if (token instanceof CloseCommentToken) {
                consumeToken(CloseCommentToken.class);
                parts.add(new CommentClose());
                break;
            } else if (token instanceof EOFToken) {
                throw new HtmlParseException("Unclosed comment");
            } else {
                var consumed = consumeToken();
                textPart.append(consumed.toString());
            }
            textPart.addTokenCount(1);
            index++;
        }
    }

    /* --------------------------- Main loop --------------------------- */

    private void readOne() {
        if (index >= tokens.size()) return;
        if (!openingNodes.isEmpty()) {
            readUntilClose(openingNodes.removeLast());
        } else {
            consumeText();
        }
    }


    /* --------------------------- DOCTYPE --------------------------- */

    private DoctypePart tryReadDoctype() {
        int save = index;
        int tokenCount = 0;

        if (!is(OpenBracketToken.class)) return null;
        skip(OpenBracketToken.class);
        tokenCount++;

        if (!(currentToken() instanceof TextToken tt) || !"!DOCTYPE".equalsIgnoreCase(tt.getText())) {
            index = save;
            return null;
        }

        skip(TextToken.class);
        tokenCount++;

        if (!(currentToken() instanceof TextToken nameTok)) {
            index = save;
            return null;
        }

        String name = nameTok.getText();
        skip(TextToken.class);
        tokenCount++;

        while (index < tokens.size() && !(currentToken() instanceof CloseBracketToken)) {
            index++;
            tokenCount++;
        }
        if (index < tokens.size()) {
            tokenCount++;
            skip(CloseBracketToken.class);
        }

        return new DoctypePart(name, tokenCount);
    }


    private void skip(Class<?> expected) {
        if (index >= tokens.size()) {
            throw error("Unexpected end of input, expected " + expected.getSimpleName());
        }
        Token tok = tokens.get(index++);
        if (!expected.isInstance(tok)) {
            throw error("Expected " + expected.getSimpleName() + " but found " + tok);
        }
    }

    private boolean is(Class<? extends Token> type) {
        return index < tokens.size() && type.isInstance(tokens.get(index));
    }

    private Token currentToken() {
        if (index >= tokens.size()) {
            return EOF;
        }
        return tokens.get(index);
    }


    private Token followingToken(int stepsAhead) {
        if (index + stepsAhead < tokens.size()) {
            return tokens.get(index + stepsAhead);
        }
        return EOF;
    }

    private HtmlParseException error(String msg) {
        return new HtmlParseException(msg);
    }

}
