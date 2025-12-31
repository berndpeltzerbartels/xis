package one.xis.html.parts;


import lombok.Data;

@Data
public class CommentPart implements Part {
    private final String text;

    @Override
    public String toString() {
        return "<!--" + text + "-->";
    }
}