package one.xis.html.tokens;

import one.xis.html.document.ParenthesisToken;

import java.util.ArrayList;
import java.util.List;

public class HtmlTokenizer {

    public List<Token> tokenize(String source) {
        List<Token> out = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int i = 0, n = source.length();

        while (i < n) {
            // Kommentare erkennen
            if (source.startsWith("<!--", i)) {
                flushText(text, out);
                out.add(new OpenCommentToken());
                i += 4;

                int end = source.indexOf("-->", i);
                if (end < 0) {
                    throw new HtmlParseException("Unclosed comment");
                }

                String commentText = source.substring(i, end);
                if (!commentText.isEmpty()) {
                    out.add(new TextToken(commentText));
                }

                out.add(new CloseCommentToken());
                i = end + 3;
                continue;
            }

            char c = source.charAt(i);

            // Spezielle Zeichen als eigene Tokens
            switch (c) {
                case '<' -> {
                    flushText(text, out);
                    out.add(new OpenBracketToken());
                    i++;
                }
                case '>' -> {
                    flushText(text, out);
                    out.add(new CloseBracketToken());
                    i++;
                }
                case '/' -> {
                    flushText(text, out);
                    out.add(new SlashToken());
                    i++;
                }
                case '=' -> {
                    flushText(text, out);
                    out.add(new EqualsToken());
                    i++;
                }
                case '"' -> {
                    flushText(text, out);
                    out.add(new ParenthesisToken());
                    i++;
                }
                case ' ', '\n' -> {
                    flushText(text, out);
                    out.add(new WhitespaceToken(c));
                    i++;
                }
                default -> {
                    // Alles andere ist Text
                    text.append(c);
                    i++;
                }
            }
        }

        flushText(text, out);
        return out;
    }

    private static void flushText(StringBuilder text, List<Token> out) {
        if (!text.isEmpty()) {
            out.add(new TextToken(text.toString()));
            text.setLength(0);
        }
    }
}
