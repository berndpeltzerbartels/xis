package one.xis.html;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.html.document.CommentNode;
import one.xis.html.document.Element;
import one.xis.html.document.Node;
import one.xis.html.document.TextNode;

import java.util.*;
import java.util.function.Supplier;

public class ElementParser {

    Element parse(String html) {
        TreeBuilder treeBuilder = new TreeBuilder(new LinkedList<>(readParts(html)));
        treeBuilder.build();
        return treeBuilder.root;
    }

    private List<ResultPart> readParts(String html) {
        PartParser partParser = new PartParser(html);
        partParser.parse();
        return partParser.getResult();
    }


    @RequiredArgsConstructor
    static class PartParser {

        private int index;
        private int mark;
        private final String html;

        private final List<Supplier<Boolean>> readerFunctions = List.of(
                this::tryReadTag,
                this::tryReadComment,
                this::consumeText,
                this::consumeTextWithOpenBracket
        );

        @Getter
        private final List<ResultPart> result = new ArrayList<>();

        void parse() {
            index = 0;
            result.clear();
            final LinkedList<Supplier<Boolean>> readers = new LinkedList<>(readerFunctions);
            while (index < html.length()) { // empty readers means no progress
                Supplier<Boolean> reader = readers.getFirst();
                if (reader.get()) {
                    readers.clear();
                    readers.addAll(readerFunctions);
                } else {
                    readers.removeFirst();
                }
            }
        }


        private boolean tryReadTag() {
            mark = index;
            if (!consume('<')) {
                return false;
            }
            boolean closing = currentChar() == '/';
            if (closing) {
                consume('/');
            }
            String name = consumeName();
            if (name == null) {
                index = mark;
                return false;
            }
            var attributes = tryReadAttributes();
            if (attributes == null) {
                index = mark;
                return false;
            }
            if (!attributes.isEmpty() && closing) {
                // closing tags cannot have attributes
                throw new IllegalStateException("Closing tag </" + name + "> cannot have attributes");
            }
            var tag = new Tag(name, attributes);
            tag.setClosing(closing);
            if (currentChar() == '/') {
                consume('/');
                tag.setEmptyTag(true);
            }
            if (SelfClosingTags.isSelfClosing(name)) {
                tag.setSelfClosing(true);
                tag.setEmptyTag(true);
            }
            if (!consume('>')) {
                index = mark;
                return false;
            }
            result.add(tag);
            return true;
        }

        private boolean tryReadComment() {
            mark = index;
            if (!consume('<')) {
                index = mark;
                return false;
            }
            if (!consume('!')) {
                index = mark;
                return false;
            }
            if (!consume('-')) {
                index = mark;
                return false;
            }
            if (!consume('-')) {
                index = mark;
                return false;
            }
            StringBuilder comment = new StringBuilder();
            while (index < html.length()) {
                if (consume('-')) {
                    if (consume('-')) {
                        if (consume('>')) {
                            result.add(new Comment(comment.toString()));
                            return true;
                        }
                    }
                } else {
                    comment.append(currentChar());
                    index++;
                }
            }
            index = mark;
            return false;
        }

        private Map<String, String> tryReadAttributes() {
            Map<String, String> attributes = new HashMap<>();
            while (index < html.length()) {
                if (!consumeWhitespaces()) {
                    return attributes;
                }
                int attributeMark = index;
                String[] nameValue = tryReadAttribute();
                if (nameValue != null) {
                    attributes.put(nameValue[0], nameValue[1]);
                    continue;
                }
                index = attributeMark;
                String booleanAttribute = tryReadBooleanAttribute();
                if (booleanAttribute != null) {
                    attributes.put(booleanAttribute, "true");
                    continue;
                }
                index = attributeMark;
                break;
            }
            consumeWhitespaces();
            return attributes;
        }

        private String[] tryReadAttribute() {
            String name = consumeName();
            if (name == null) {
                return null;
            }
            if (!consume('=')) {
                return null;
            }
            if (!consume('"')) {
                return null;
            }
            String value = consumeString();
            if (value == null) {
                return null;
            }
            return new String[]{name, value};
        }

        private String tryReadBooleanAttribute() {
            return consumeName();
        }

        private boolean consumeWhitespaces() {
            boolean consumed = false;
            while (index < html.length() && Character.isWhitespace(html.charAt(index))) {
                index++;
                consumed = true;
            }
            return consumed;
        }

        private String consumeString() {
            StringBuilder s = new StringBuilder();
            while (index < html.length()) {
                char c = html.charAt(index);
                if (c == '"') {
                    index++;
                    return s.toString();
                } else {
                    if (consume(c)) {
                        s.append(c);
                    }
                }
            }
            return null;
        }

        private char currentChar() {
            return html.charAt(index);
        }

        private boolean consume(char c) {
            if (html.length() > index && html.charAt(index) == c) {
                index++;
                return true;
            }
            return false;
        }

        private String consumeName() {
            StringBuilder s = new StringBuilder();
            boolean namespace = false;
            while (index < html.length()) {
                char c = html.charAt(index);
                if (c == ':') {
                    if (namespace) {
                        throw new IllegalStateException("Multiple namespaces not supported");
                    }
                    namespace = true;
                    s.append(c);
                } else if (isNameChar(c)) {
                    s.append(c);

                } else {
                    break;
                }
                index++;
            }
            if (s.isEmpty()) {
                return null;
            }
            return s.toString();
        }

        private boolean isNameChar(char c) {
            return Character.isLetterOrDigit(c) || c == '-' || c == '_' || c == '.';

        }

        private boolean consumeTextWithOpenBracket() {
            consume('<');
            StringBuilder s = new StringBuilder();
            s.append('<');
            return consumeText(s);
        }

        private boolean consumeText() {
            return consumeText(new StringBuilder());
        }

        private boolean consumeText(StringBuilder s) {
            boolean escaped = false;
            while (index < html.length()) {
                char c = html.charAt(index);
                if (c == '\\') {
                    escaped = !escaped;
                    s.append(c);
                    index++;
                    continue;
                }
                if (escaped) {
                    escaped = false;
                    s.append(c);
                    index++;
                    continue;
                }
                if (c == '<') {
                    break;
                } else {
                    s.append(c);
                    index++;
                }
            }
            if (s.isEmpty()) {
                return false;
            }
            result.add(new Text(s.toString()));
            return true;
        }
    }

    interface ClosableNode {
    }

    public interface ResultPart {
    }


    @Data
    static class Tag implements ResultPart, ClosableNode {
        private final String name;
        private final Map<String, String> attributes;

        /**
         * * True if the tag is self-closing in HTML, e.g. <br>
         */
        private boolean selfClosing;
        /**
         * * True if the tag is a closing tag, e.g. </tag>
         */
        private boolean closing;
        /**
         * * True if the tag has no content in xml-style, e.g. <tag/>
         */
        private boolean emptyTag;

        /**
         * Tag is closed in XML-style, e.g. <tag/> or is self-closing in HTML-style, e.g. <br>
         *
         * @return true in case of closed tag
         */
        boolean isClosed() {
            return selfClosing || emptyTag;
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            builder.append("<");
            if (closing) {
                builder.append("/");
            }
            builder.append(name);
            attributes.forEach((k, v) -> builder.append(" ").append(k).append("=\"").append(v).append("\""));
            if (emptyTag) {
                builder.append(" /");
            }
            builder.append(">");
            return builder.toString();
        }
    }

    @Data
    static class Comment implements ResultPart, ClosableNode {
        private final String text;

        @Override
        public String toString() {
            return "<!--" + text + "-->";
        }
    }

    static class Text implements ResultPart {
        private final StringBuilder text;

        Text(String initialText) {
            this.text = new StringBuilder(initialText);
        }

        String getText() {
            return text.toString();
        }

        void append(String moreText) {
            text.append(moreText);
        }

        public boolean isWhitespaceOnly() {
            return text.toString().trim().isEmpty();
        }

        @Override
        public String toString() {
            return text.toString();
        }
    }

    @RequiredArgsConstructor
    static class TreeBuilder {
        private final LinkedList<ResultPart> parts;


        @Getter
        private Element root;
        private final LinkedList<ClosableNode> openTags = new LinkedList<>();

        void build() {
            if (parts.isEmpty()) {
                throw new HtmlParseException("Cannot build element from empty document");
            }
            trim();
            evaluate();
        }

        private void evaluate() {
            skipWhitespaces();
            if (parts.isEmpty()) {
                throw new HtmlParseException("Cannot build element tree: no root element found");
            }

            ResultPart part = parts.removeFirst();
            if (part instanceof Tag tag) {
                root = toElement(tag);
                if (!tag.isClosed()) {
                    openTags.add(tag);
                    evaluateChild(root);
                    evaluateSibling(root); // for syntax checks, should fail if there is extra content
                    if (root.getNextSibling() != null) {
                        throw new HtmlParseException("Expected exactly one top-level element, found extra content after it");
                    }
                    if (!openTags.isEmpty()) {
                        throw new HtmlParseException("No closing tag found for : " + openTags);
                    }
                }
                return;
            }
            throw new HtmlParseException("There should be no content outside of root element: " + part);

        }


        private void skipWhitespaces() {
            while (!parts.isEmpty()) {
                ResultPart part = parts.getFirst();
                if (part instanceof Text text && text.isWhitespaceOnly()) {
                    parts.removeFirst();
                } else {
                    break;
                }
            }
        }


        private void evaluateSibling(Node node) {
            if (parts.isEmpty()) {
                return;
            }
            ResultPart part = parts.removeFirst();
            if (part instanceof Tag tag) {
                if (tag.isClosing()) {
                    checkClosingTag(tag);
                    return;
                }
                Element element = toElement(tag);
                node.setNextSibling(element);
                element.setParentNode(node.getParentNode());
                if (tag.isClosed()) {
                    evaluateSibling(element);
                } else {
                    openTags.add(tag);
                    evaluateChild(element);
                    evaluateSibling(element);
                }
            } else if (part instanceof Comment comment) {
                CommentNode commentNode = toCommentNode(comment);
                node.setNextSibling(commentNode);
                commentNode.setParentNode(node.getParentNode());
                evaluateSibling(commentNode);
            } else if (part instanceof Text textPart) {
                if (!textPart.isWhitespaceOnly()) {
                    Node textNode = new one.xis.html.document.TextNode(textPart.getText());
                    node.setNextSibling(textNode);
                    textNode.setParentNode(node.getParentNode());
                    evaluateSibling(textNode);
                } else {
                    evaluateSibling(node);
                }
            }
        }

        private void evaluateChild(Element parent) {
            if (parts.isEmpty()) {
                return;
            }
            ResultPart part = parts.removeFirst();
            if (part instanceof Tag tag) {
                if (tag.isClosing()) {
                    checkClosingTag(tag);
                    return;
                }
                Element element = toElement(tag);
                parent.appendChild(element);
                element.setParentNode(parent);
                if (tag.isClosed()) {
                    evaluateSibling(element);
                } else {
                    openTags.add(tag);
                    evaluateChild(element);
                    evaluateSibling(element);
                }
            } else if (part instanceof Comment comment) {
                CommentNode commentNode = toCommentNode(comment);
                parent.appendChild(commentNode);
                commentNode.setParentNode(parent);
                evaluateSibling(commentNode);
            } else if (part instanceof Text textPart) {
                if (!textPart.isWhitespaceOnly()) {
                    Node textNode = new TextNode(textPart.getText());
                    parent.appendChild(textNode);
                    textNode.setParentNode(parent);
                    evaluateSibling(textNode);
                } else {
                    evaluateChild(parent);
                }
            }
        }


        private void checkClosingTag(Tag closingTag) {
            if (openTags.isEmpty()) {
                throw new HtmlParseException("Unexpected closing tag </" + closingTag.getName() + "> with no matching opening tag");
            }
            Tag openTag = (Tag) openTags.removeLast();
            if (!openTag.getName().equalsIgnoreCase(closingTag.getName())) {
                throw new HtmlParseException("Mismatched closing tag: expected </" + openTag.getName() + "> but found </" + closingTag.getName() + ">");
            }
        }


        private void trim() {
            while (!parts.isEmpty()) {
                ResultPart first = parts.getFirst();
                if (first instanceof Text text && text.isWhitespaceOnly() || first instanceof Comment) {
                    parts.removeFirst();
                } else {
                    break;
                }
            }
            while (!parts.isEmpty()) {
                ResultPart last = parts.getLast();
                if (last instanceof Text text && text.isWhitespaceOnly() || last instanceof Comment) {
                    parts.removeLast();
                } else {
                    break;
                }
            }
        }


        private Element toElement(Tag tag) {
            Element element = new Element(tag.getName());
            tag.getAttributes().forEach(element::setAttribute);
            return element;
        }

        private CommentNode toCommentNode(Comment comment) {
            return new CommentNode(comment.getText());
        }
    }

    static class SelfClosingTags {
        private static final Set<String> SELF_CLOSING_TAGS = Set.of(
                "area", "base", "br", "col", "embed", "hr", "img",
                "input", "link", "meta", "param", "source", "track", "wbr"
        );

        public static boolean isSelfClosing(String tagName) {
            return SELF_CLOSING_TAGS.contains(tagName.toLowerCase());
        }
    }
}
