package one.xis.template;

import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Data
public class TemplateModel {

    private final Collection<String> dataVarNames;
    private final XmlElement root;

    public interface TemplateElement {
    }


    public interface Container {
        List<TemplateElement> getElements();

        void addElement(TemplateElement e);
    }

    @Data
    public static class IfElement implements TemplateElement, Container {
        private final String condition;
        private final List<TemplateElement> childElements;

        @Override
        public List<TemplateElement> getElements() {
            return childElements;
        }

        @Override
        public void addElement(TemplateElement e) {
            childElements.add(e);
        }
    }

    @Data
    public static class ForElement implements TemplateElement {
        private final String arrayVarName;
        private final String elementVarName;
        private final String indexVarName;
        private final List<TemplateElement> childElements;
    }

    /**
     * Contains static strings and expressions.
     */
    @Data
    public static class TextContent implements TemplateElement {
        private final List<ContentElement> contentElements;
    }

    @Data
    public static class XmlElement implements TemplateElement {
        private final String tagName;
        private final Map<String, TextContent> attributes;
        private final List<TemplateElement> childElements;
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

    }

    @Data
    static class Expression implements TemplateElement, ContentElement {
        private final String content;
    }
}
