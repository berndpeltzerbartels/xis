package one.xis.template;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class TemplateModel {

    private final Collection<String> dataVarNames;
    private final TemplateElement root;

    public interface TemplateElement {
    }


    public interface Container extends TemplateElement {
        List<TemplateElement> getElements();

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
    public static class TextContent implements TemplateElement {
        private final List<ContentElement> contentElements;

        @Override
        public String toString() {
            return contentElements.stream().map(ContentElement::toString).collect(Collectors.joining(""));
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
     * <ul>
     * <li>Body-conent of elements</li>
     * <li>Attributes-Keys</li>
     * <li>Attributes-Keys</li>
     * </ul>
     */
    public interface ContentElement extends TemplateElement {
    }


    @Data
    static class StaticContent implements TemplateElement, ContentElement {
        private final List<String> lines;

        StaticContent(String s) {
            lines = List.of(s);
        }

        StaticContent(List<String> lines) {
            this.lines = lines;
        }

        @Override
        public String toString() {
            return lines.stream().collect(Collectors.joining("\\\n"));
        }
    }

    @Data
    static class Expression implements TemplateElement, ContentElement {
        private final String content;

        @Override
        public String toString() {
            return "<%=" + content + ">";
        }
    }


}
