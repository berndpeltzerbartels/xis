package one.xis.template;

import lombok.RequiredArgsConstructor;
import one.xis.template.TemplateModel.*;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TemplateParser {

    private final Document document;
    private final Collection<String> vars;
    private int varIndex = 0;
    private static final String ATTR_IF = "data-if";
    private static final String ATTR_FOR = "data-for";
    private static final String ATTR_LOOP_INDEX = "data-index";
    private static final Pattern EXPR_PATTERN = Pattern.compile("\\$\\{[^}]+\\}");


    TemplateModel parse(Document document, Collection<String> dataVarNames) throws TemplateSynthaxException, IOException {
        return new TemplateModel(dataVarNames, parseElement(document.getDocumentElement()));
    }

    private TemplateElement parse(Node node) {
        if (node instanceof Element) {
            Element e = (Element) node;
            if (e.getAttribute("data-if") != null) {
                return parseIf(e);
            } else if (e.getAttribute("data-for") != null) {
                return parseFor(e);
            }
        }
        return null;
    }

    private IfElement parseIf(Element src) {
        if (src.getAttribute("data-else") != null) {
            return new IfElement(src.getAttribute("data-if"), List.of(parseFor(src)));
        }
        return new IfElement(src.getAttribute("data-if"), parseChildren(src));
    }

    private ForElement parseFor(Element src) {
        String[] array = src.getAttribute("data-for").split(":");
        // TODO lenght == 2 !
        return new ForElement(array[1], array[0], getIndexVarName(src), parseChildren(src));
    }

    private XmlElement parseElement(Element src) {
        return new XmlElement(src.getTagName(), parseAttributes(src), parseChildren(src));
    }

    private List<TemplateElement> parseChildren(Element src) {
        return XmlUtil.getChildNodes(src).map(this::parse).collect(Collectors.toList());
    }

    private Map<MixedContent, MixedContent> parseAttributes(Element src) {
        return null;
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

    private void writeIf(Element e, Appendable out) throws IOException {
        out.append("if (");
        out.append(e.getAttribute(ATTR_IF));
        out.append("){");
    }

    private void writeFor(Element e, Appendable out) throws TemplateSynthaxException, IOException {
        String[] s = e.getAttribute(ATTR_FOR).split(":");
        if (s.length != 2) {
            throw new TemplateSynthaxException(e, "invalid value for " + ATTR_FOR);
        }
        String arrayName = validateVariableName(s[1].trim());
        String varName = validateVariableName(s[0].trim());
        String indexName = getIndexVarName(e);

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


    private String getIndexVarName(Element e) {
        return e.getAttribute(ATTR_LOOP_INDEX) != null ? e.getAttribute(ATTR_LOOP_INDEX) : nextVarName();
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
