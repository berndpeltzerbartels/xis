package one.xis.template;


public class Expression implements TextElement {
    private final String content;

    public Expression(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "<%=" + content + ">";
    }
}
