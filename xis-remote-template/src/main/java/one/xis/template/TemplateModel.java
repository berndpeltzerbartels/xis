package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class TemplateModel {

    private final TemplateElement root;
    private final String name;

    public interface TemplateElement {

    }


    public interface Container extends TemplateElement {
        List<? extends TemplateElement> getElements();

        void addElement(TemplateElement e);
    }

    @Data
    public static class IfElement implements Container {
        private final String condition;
        private final List<TemplateElement> childElements = new ArrayList<>();

        @Override
        public List<TemplateElement> getElements() {
            return childElements;
        }

        @Override
        public void addElement(TemplateElement e) {
            childElements.add(e);
        }

        @Override
        public String toString() {
            return "if(" + condition + ")";
        }
    }

    @Data
    public static class ForElement implements Container {
        private final String arrayVarName;
        private final String elementVarName;
        private final String indexVarName;
        private final List<TemplateElement> childElements = new ArrayList<>();

        @Override
        public List<TemplateElement> getElements() {
            return childElements;
        }

        @Override
        public void addElement(TemplateElement e) {
            childElements.add(e);
        }

        @Override
        public String toString() {
            return "for(" + elementVarName + ":" + arrayVarName + ")";
        }
    }

    /**
     * Contains static strings and expressions.
     */
    @Data
    public static class TextContent implements Container {
        private final List<TextElement> textElements = new ArrayList<>();

        @Override
        public String toString() {
            return textElements.stream().map(TextElement::toString).collect(Collectors.joining(""));
        }

        @Override
        public List<? extends TemplateElement> getElements() {
            return textElements;
        }

        @Override
        public void addElement(TemplateElement e) {
            if (e instanceof TextElement) {
                textElements.add((TextElement) e);
            }
            throw new IllegalArgumentException("TextContent must contain text-elements, only and not " + e);
        }

        public void addAllElements(Collection<TextElement> textElements) {
            this.textElements.addAll(textElements);
        }
    }

    @Data
    public static class XmlElement implements Container {
        private final String tagName;
        private final Map<String, TextContent> attributes;
        private final List<TemplateElement> childElements = new ArrayList<>();

        @Override
        public List<TemplateElement> getElements() {
            return childElements;
        }

        @Override
        public void addElement(TemplateElement e) {
            childElements.add(e);
        }

        @Override
        public String toString() {
            return "<" + tagName + ">";
        }
    }

    /**
     * Textcontent, might be static text or expression (variable-output)
     * Found in:
     * <ul>
     * <li>Body-Content of XML-elements</li>
     * <li>XML-attributes-values</li>
     * </ul>
     */
    public interface TextElement extends TemplateElement {
    }


    @Data
    public static class StaticText implements TextElement {
        private final List<String> lines;

        StaticText(String s) {
            lines = List.of(s);
        }

        StaticText(List<String> lines) {
            this.lines = lines;
        }

        @Override
        public String toString() {
            return lines.stream().collect(Collectors.joining("\\\n"));
        }
    }

    @Data
    public static class Expression implements TextElement {
        private final String content;

        @Override
        public String toString() {
            return "<%=" + content + ">";
        }
    }


}
