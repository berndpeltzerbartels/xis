package one.xis.template;

import lombok.RequiredArgsConstructor;
import one.xis.utils.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

@RequiredArgsConstructor
public class JavascriptWriter {

    private final Document document;
    private final Collection<String> vars;
    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final Pattern EXPR_PATTERN = Pattern.compile("\\$\\{[^}]+\\}");

    private XmlSerializer serializer = new XmlSerializer();

    void serialize(Appendable out) throws TemplateSynthaxException, IOException {
        out.append("this.getContent = function(data) {");
        out.append("\n");
        out.append("var content = \"\";\n");
        for (String var : vars) {
            out.append("var ");
            out.append(var);
            out.append("=data.");
            out.append(var);
            out.append(";");
            out.append("\n");
        }
        writeElement(document.getDocumentElement(), out);
        out.append("return content;\n");
        out.append("}\n");
    }


    private void writeElement(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        int blockCount = 0;
        if (isIf(e)) {
            writeIf(e, out);
            blockCount++;
        }
        if (isFor(e)) {
            writeFor(e, out);
            blockCount++;
        }
        writeStartTag(e, out);
        if (hasTextContent(e)) {
            writeTextContent(e, out);
        }
        writeCloseTag(e, out);
        writeBlockEnd(blockCount, out);
    }

    private void writeIf(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        out.append("if (");
        out.append(e.getAttribute(ATTR_IF));
        out.append("){");
    }

    private void writeFor(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        String[] s = e.getAttribute(ATTR_FOR).split(":");
        if (s.length < 2 || s.length > 3) {
            throw new TemplateSynthaxException(e, "invalid value for " + ATTR_FOR);
        }
        String arrayName = validateVariableName(s[1].trim());
        String varName = validateVariableName(s[0].trim());
        String indexName = s.length > 2 ? s[2] : nextVarName();

        out.append("for (var ");
        out.append(indexName);
        out.append("=0;");
        out.append(indexName);
        out.append("<");
        out.append(arrayName);
        out.append(".length;");
        out.append(indexName);
        out.append("++){\n");

        out.append("var ");
        out.append(varName);
        out.append("=");
        out.append(arrayName);
        out.append("[");
        out.append(indexName);
        out.append("];\n");
    }

    private void writeBlockEnd(int count, Appendable out) throws TemplateSynthaxException, IOException {
        for (int i = 0; i < count; i++) {
            out.append("}\n");
        }
    }

    private void writeStartTag(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        out.append("content+=");
        out.append("\"<");
        out.append(e.getTagName());
        Iterator<Map.Entry<String, String>> iterator = XmlUtil.getAttributes(e).entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String name = entry.getKey();
            String value = entry.getValue();
            out.append(" ");
            writeTextWithVariables(name, out);
            out.append("=\\\"");
            writeTextWithVariables(value, out);
            out.append("\\\"");
        }
        out.append(">\";\n");
    }

    private void writeCloseTag(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        out.append("content+=");
        out.append("\"</");
        out.append(e.getTagName());
        out.append(">\";\n");
    }


    private void writeTextContent(Element e, Appendable out) throws TemplateSynthaxException {
        writeTextWithVariables(e.getTextContent(), out);
    }

    private void writeTextWithVariables(String text, Appendable out) throws TemplateSynthaxException {

    }

    private boolean isIf(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_IF));
    }

    private boolean isFor(Element e) {
        return StringUtils.isNotEmpty(e.getAttribute(ATTR_FOR));
    }

    private boolean hasTextContent(Element e) {
        return !StringUtils.trimNullSafe(e.getTextContent()).isEmpty();

    }

    private String validateVariableName(String name) {
        return name;
    }

    private String parseCondition(Element e) {
        return null;
    }

    private String nextVarName() {
        return "var" + (varIndex++);
    }

}
