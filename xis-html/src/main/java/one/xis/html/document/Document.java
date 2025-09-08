package one.xis.html.document;

import lombok.Data;

@Data
public class Document {
    private Doctype doctype;
    private Element documentElement;

    public String asString() {
        StringBuilder sb = new StringBuilder();
        if (doctype != null) {
            sb.append("<!DOCTYPE ").append(doctype.getName()).append(">\n");
        }
        sb.append(documentElement.asString());
        return sb.toString();
    }
}
