package one.xis.jsc;

import lombok.NonNull;
import one.xis.js.JSScript;
import one.xis.js.JSWriter;
import one.xis.template.TemplateSynthaxException;
import one.xis.utils.lang.StringUtils;
import one.xis.utils.xml.XmlUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.IOException;

import static one.xis.path.PathUtils.getFile;
import static one.xis.path.PathUtils.getSuffix;

class JavascriptComponentUtils {

    static String javaScriptModelAsCode(@NonNull JSScript script) {
        StringBuilder builder = new StringBuilder();
        JSWriter jsWriter = new JSWriter(builder);
        jsWriter.write(script);
        return builder.toString();
    }

    static String urnToClassName(String urn) {
        return urn.replace(':', '.');
    }

    static String nameToUrn(String name) {
        return name.replace('.', ':');
    }

    static String pathToUrn(String name) {
        return name.replace('/', ':');
    }

    static String urnToPath(String name) {
        return name.replace(':', '/');
    }


    static Document htmlToDocument(String controllerClass, String htmlSource) {
        try {
            return htmlToDocument(htmlSource);
        } catch (SAXException e) {
            throw new TemplateSynthaxException(controllerClass + ": " + e.getMessage());
        }
    }

    static String getHtmlTemplatePath(Class<?> controllerClass) {
        return controllerClass.getName().replace('.', '/') + ".html";
    }


    static void validatePath(String path) {
        String file = getFile(path);
        if (StringUtils.getNumberOfOccurences(file, '.') > 1) {
            throw new IllegalStateException(String.format("file in path '%s' must not have more than one occurences of '.'", path));
        }
        String suffix = getSuffix(path);
        if (suffix != null && !"html".equals(suffix)) {
            throw new IllegalStateException(String.format("path '%s' in @PageJavascript must have suffix 'html' or no one", path));
        }
    }

    private static Document htmlToDocument(String htmlSource) throws SAXException {
        try {
            return XmlUtil.loadDocument(htmlSource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
