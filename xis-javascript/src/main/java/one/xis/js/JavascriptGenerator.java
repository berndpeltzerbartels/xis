package one.xis.js;

import one.xis.template.ExpressionParser;
import one.xis.template.PageModel;
import one.xis.template.TemplateParser;
import one.xis.template.WidgetTemplateModel;
import one.xis.utils.io.IOUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class JavascriptGenerator {

    private static final String WIDGET_INFO_PATH = "META-INF/xis/widgets";
    private static final String PAGES_INFO_PATH = "META-INF/xis/pages";

    private final TemplateParser templateParser = new TemplateParser(new ExpressionParser());
    private final JavascriptParser javascriptParser = new JavascriptParser();

    String generateJavascript() {
        StringWriter stringWriter = new StringWriter();
        try (PrintWriter writer = new PrintWriter(stringWriter)) {
            JSWriter javasscriptWriter = new JSWriter(writer); // TODO better Appendable ?
            javasscriptWriter.write(createScriptModel());
        }
        return stringWriter.toString();
    }

    private JSScript createScriptModel() {
        Collection<PageModel> pageModels = pageModels();
        Collection<WidgetTemplateModel> widgetTemplateModels = widgetModels();
        javascriptParser.parse(pageModels, widgetTemplateModels);
        return javascriptParser.getScript();
    }

    private Collection<WidgetTemplateModel> widgetModels() {
        return lines(WIDGET_INFO_PATH)
                .map(line -> line.split(":"))
                .map(arr -> widgetModel(arr[0], arr[1]))
                .collect(Collectors.toSet());
    }

    private Collection<PageModel> pageModels() {
        return lines(PAGES_INFO_PATH)
                .map(line -> line.split(":"))
                .map(arr -> pageModel(arr[0], arr[1]))
                .collect(Collectors.toSet());
    }

    private Stream<String> lines(String resource) {
        return IOUtils.getContentLines(IOUtils.getResourceAsStream(resource), "utf-8").stream();
    }

    private WidgetTemplateModel widgetModel(String name, String resource) {
        return templateParser.parseWidgetTemplate(loadDocument(resource), name);
    }

    private PageModel pageModel(String path, String resource) {
        return templateParser.parsePage(loadDocument(resource), path); // TODO may be an alias is better to avoid duplicates
    }

    private Document loadDocument(String resource) {
        try {
            return XmlUtil.loadDocument(IOUtils.getResourceAsStream(resource));
        } catch (Exception e) {
            throw new RuntimeException(e); // TODO more info
        }
    }
}
