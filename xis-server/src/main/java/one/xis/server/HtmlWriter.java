package one.xis.server;

import org.dom4j.io.OutputFormat;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Set;

public class HtmlWriter extends org.dom4j.io.HTMLWriter {

    private static final Set<String> SELF_CLOSING_TAGS = Set.of(
            "area", "base", "br", "col", "command", "embed", "hr", "img",
            "input", "keygen", "link", "meta", "param", "source", "track",
            "wbr"
    );

    public HtmlWriter() throws UnsupportedEncodingException {
    }

    public HtmlWriter(OutputFormat format) throws UnsupportedEncodingException {
        super(format);
    }

    public HtmlWriter(OutputStream out) throws UnsupportedEncodingException {
        super(out);
    }

    public HtmlWriter(OutputStream out, OutputFormat format) throws UnsupportedEncodingException {
        super(out, format);
    }

    public HtmlWriter(Writer writer) {
        super(writer);
    }

    public HtmlWriter(Writer writer, OutputFormat format) {
        super(writer, format);
    }

    @Override
    protected void writeEmptyElementClose(String qualifiedName) throws IOException {
        if (SELF_CLOSING_TAGS.contains(qualifiedName.toLowerCase())) {
            writer.write("/>");
            return;
        }
        writer.write("></" + qualifiedName + ">"); // Write as a normal tag with closing

    }
}
