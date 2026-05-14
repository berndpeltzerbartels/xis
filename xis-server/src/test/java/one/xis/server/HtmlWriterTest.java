// Datei: xis-server/src/test/java/one/xis/server/HtmlWriterTest.java
package one.xis.server;

import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HtmlWriterTest {

    @Test
    void testNonSelfClosingTag() throws Exception {
        var doc = DocumentFactory.getInstance().createDocument();
        Element textarea = doc.addElement("textarea");
        StringWriter writer = new StringWriter();
        HtmlWriter htmlWriter = new HtmlWriter(writer, OutputFormat.createPrettyPrint());
        htmlWriter.write(textarea);
        htmlWriter.flush();
        String result = writer.toString();
        assertTrue(result.contains("<textarea></textarea>"));
    }

    @Test
    void testSelfClosingTag() throws Exception {
        var doc = DocumentFactory.getInstance().createDocument();
        Element br = doc.addElement("br");
        StringWriter writer = new StringWriter();
        HtmlWriter htmlWriter = new HtmlWriter(writer, OutputFormat.createPrettyPrint());
        htmlWriter.write(br);
        htmlWriter.flush();
        String result = writer.toString();
        assertTrue(result.contains("<br/>"));
    }


    @Test
    void testNonSelfClosingTagWithAttributes() throws Exception {
        var doc = DocumentFactory.getInstance().createDocument();
        Element textarea = doc.addElement("textarea");
        textarea.addAttribute("id", "myTextarea");
        textarea.addAttribute("class", "form-control");
        StringWriter writer = new StringWriter();
        HtmlWriter htmlWriter = new HtmlWriter(writer, OutputFormat.createPrettyPrint());
        htmlWriter.write(textarea);
        htmlWriter.flush();
        String result = writer.toString();
        assertTrue(result.contains("<textarea id=\"myTextarea\" class=\"form-control\"></textarea>"));
    }

    @Test
    void testSelfClosingTagWithAttributes() throws Exception {
        var doc = DocumentFactory.getInstance().createDocument();
        Element br = doc.addElement("br");
        br.addAttribute("class", "myBr");
        StringWriter writer = new StringWriter();
        HtmlWriter htmlWriter = new HtmlWriter(writer, OutputFormat.createPrettyPrint());
        htmlWriter.write(br);
        htmlWriter.flush();
        String result = writer.toString();
        assertTrue(result.contains("<br class=\"myBr\"/>"));
    }
}