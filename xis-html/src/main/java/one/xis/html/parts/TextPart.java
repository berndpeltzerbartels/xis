package one.xis.html.parts;

import lombok.Data;

@Data
public class TextPart implements Part {
    private final StringBuilder text;
    private int tokenCount;

    TextPart(String initialText) {
        this.text = new StringBuilder(initialText);
    }

    TextPart() {
        this.text = new StringBuilder();
    }

    void append(String s) {
        text.append(s);
    }

    public String getText() {
        return text.toString();
    }

    @Override
    public String toString() {
        return text.toString();
    }

    @Override
    public int tokenCount() {
        return tokenCount;
    }

    public void addTokenCount(int i) {
        tokenCount += i;
    }
}
