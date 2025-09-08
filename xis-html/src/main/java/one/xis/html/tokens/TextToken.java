package one.xis.html.tokens;


import lombok.Data;

@Data
public class TextToken implements Token {
    private final String text;

    @Override
    public String toString() {
        return text;
    }
}
