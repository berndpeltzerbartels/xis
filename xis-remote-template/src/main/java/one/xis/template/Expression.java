package one.xis.template;

import lombok.Data;

@Data
public class Expression implements TextElement {
    private final String content;

    @Override
    public String toString() {
        return "<%=" + content + ">";
    }
}
