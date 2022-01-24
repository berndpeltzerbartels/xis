package one.xis.template;

import lombok.NonNull;
import one.xis.template.TemplateModel.*;

import java.io.IOException;
import java.util.*;

public class TemplateJavascriptWriter {

    private static Map<Class<? extends TemplateElement>, ElementWriter> writers = new HashMap<>();

    static {
        writers.put(IfElement.class, new IfElementWriter());
        writers.put(ForElement.class, new ForElementWriter());
        writers.put(TextContent.class, new MixedContentWriter());
        writers.put(XmlElement.class, new XmlElementWriter());
        writers.put(StaticText.class, new StaticContentWriter());
        writers.put(TemplateModel.Expression.class, new ExpressionWriter());
    }

    private interface ElementWriter<E extends TemplateElement> {

        void doWrite(E e, Appendable out) throws IOException;

    }


    void writeContentMethod(TemplateModel templateModel, Appendable out) throws IOException {
        writeMethodHead(out);
        writeVariables(templateModel.getDataVarNames(), out);
        writeElements(List.of(templateModel.getRoot()), out);
        out.append("return content;\n");
    }

    @SuppressWarnings("unchecked")
    static <E extends TemplateElement> void writeElements(List<TemplateElement> elements, Appendable out) throws IOException {
        for (TemplateElement e : elements) {
            ElementWriter<E> writer = (ElementWriter<E>) writerFor(e.getClass());
            writer.doWrite((E) e, out);
        }
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private static <E extends TemplateElement> ElementWriter<E> writerFor(Class<E> e) {
        return Optional.ofNullable(writers.get(e.getClass())).orElseThrow(() -> new IllegalStateException("no writer for " + e));
    }


    private void writeMethodHead(Appendable out) throws IOException {
        out.append("function createContent(data) {");
    }


    private void writeVariables(Collection<String> names, Appendable out) throws IOException {
        out.append("var content = \"\";\n");
        for (String varName : names) {
            out.append("var ");
            out.append(varName);
            out.append("=data.");
            out.append(varName);
            out.append(";");
            out.append("\n");
        }
    }

    private static class IfElementWriter implements ElementWriter<IfElement> {

        @Override
        public void doWrite(IfElement e, Appendable out) throws IOException {
            out.append("if (");
            out.append(e.getCondition());
            out.append("){");
            writeElements(e.getChildElements(), out);
            out.append("}");
        }
    }

    private static class ForElementWriter implements ElementWriter<ForElement> {

        @Override
        public void doWrite(ForElement e, Appendable out) throws IOException {
            out.append("for (var ");
            out.append(e.getIndexVarName());
            out.append("=0;");
            out.append(e.getIndexVarName());
            out.append("<");
            out.append(e.getArrayVarName());
            out.append(".length;");
            out.append(e.getIndexVarName());
            out.append("++){\n");
            out.append("var ");
            out.append(e.getElementVarName());
            out.append("=");
            out.append(e.getArrayVarName());
            out.append("[");
            out.append(e.getIndexVarName());
            out.append("];\n");
            writeElements(e.getChildElements(), out);
            out.append("}");
        }
    }

    private static class MixedContentWriter implements ElementWriter<TextContent> {

        @Override
        public void doWrite(TextContent content, Appendable out) throws IOException {
            for (TextElement textElement : content.getTextElements()) {
                write(textElement, out);
            }
        }

        @SuppressWarnings("unchecked")
        private <E extends TextElement> void write(TextElement e, Appendable out) throws IOException {
            ElementWriter<E> writer = (ElementWriter<E>) writerFor(e.getClass());
            writer.doWrite((E) e, out);
        }
    }

    private static class XmlElementWriter implements ElementWriter<XmlElement> {

        @Override
        public void doWrite(XmlElement xmlElement, Appendable out) throws IOException {
            writeStartTag(xmlElement, out);
            writeElements(xmlElement.getChildElements(), out);
            writeEndTag(xmlElement, out);
        }

        private void writeStartTag(XmlElement e, Appendable out) throws IOException {
            ElementWriter<TextContent> textContentWriter = writerFor(TextContent.class);
            out.append("content+=");
            out.append("\"<");
            out.append(e.getTagName());
            Iterator<Map.Entry<String, TextContent>> iterator = e.getAttributes().entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, TextContent> entry = iterator.next();
                String name = entry.getKey();
                TextContent value = entry.getValue();
                out.append(" ");
                out.append(name);
                out.append("=\\\"");
                textContentWriter.doWrite(value, out);
                out.append("\\\"");
            }
            out.append(">\";\n");
        }

        private void writeEndTag(XmlElement e, Appendable out) throws IOException {
            out.append("content+=");
            out.append("\"</");
            out.append(e.getTagName());
            out.append(">\";\n");
        }

    }

    private static class StaticContentWriter implements ElementWriter<StaticText> {

        @Override
        public void doWrite(StaticText content, Appendable out) throws IOException {
            for (String line : content.getLines()) {
                out.append("content+=");
                out.append("\"");
                out.append(line);
                out.append("\";\n");
            }
        }
    }

    private static class ExpressionWriter implements ElementWriter<TemplateModel.Expression> {

        @Override
        public void doWrite(TemplateModel.Expression expression, Appendable out) throws IOException {
            out.append("content+=");
            out.append(expression.getContent());
            out.append(";\n");
        }
    }

}
