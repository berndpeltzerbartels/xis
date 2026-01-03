package one.xis.html.tokens;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class WhitespaceToken implements Token {
    private final char whitespaceChar;

    @Override
    public String toString() {
        return Character.toString(whitespaceChar);
    }
}
