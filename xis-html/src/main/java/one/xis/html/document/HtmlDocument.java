package one.xis.html.document;

import lombok.Data;
import one.xis.html.HtmlParser;

@Data
public class HtmlDocument {
    private Doctype doctype;
    private Element documentElement;

    public static HtmlDocument of(String html) {
        return new HtmlParser().parse(html);
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        if (doctype != null) {
            sb.append("<!DOCTYPE ").append(doctype.getName()).append(">\n");
        }
        sb.append(documentElement.toHtml());
        return sb.toString();
    }
}
