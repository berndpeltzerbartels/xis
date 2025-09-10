package one.xis.html.tokens;

import java.util.ArrayList;
import java.util.List;

public class HtmlTokenizer {

    public List<Token> tokenize(String source) {
        List<Token> out = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        boolean inTag = false;
        int i = 0, n = source.length();

        while (i < n) {
            char c = source.charAt(i);

            if (!inTag) {
                // TEXT-MODUS: alles bis zum nächsten '<' sammeln (inkl. Leerzeichen)
                if (c == '<') {
                    flushText(text, out);
                    out.add(new OpenBracketToken());
                    inTag = true;
                    i++;
                } else {
                    text.append(c);
                    i++;
                }
                continue;
            }

            // TAG-MODUS (zwischen '<' und '>')
            switch (c) {
                case '>' -> {
                    out.add(new CloseBracketToken());
                    inTag = false;
                    i++;
                }
                case '/' -> {
                    out.add(new SlashToken());
                    i++;
                }
                case '=' -> {
                    out.add(new EqualsToken());
                    i++;
                }
                case '"', '\'' -> {
                    // QUOTED STRING als EIN TextToken (ohne Anführungszeichen)
                    char quote = c;
                    i++; // öffnendes Quote überspringen
                    StringBuilder q = new StringBuilder();
                    while (i < n) {
                        char qc = source.charAt(i++);
                        if (qc == quote) break;
                        q.append(qc);
                    }
                    out.add(new TextToken(q.toString()));
                }
                default -> {
                    if (isSpace(c)) {
                        // Whitespace im Tagkopf = Separator -> ignorieren
                        i++;
                    } else {
                        // Unquoted Name/Value: bis zu Trennzeichen lesen
                        StringBuilder sb = new StringBuilder();
                        sb.append(c);
                        i++;
                        while (i < n) {
                            char x = source.charAt(i);
                            if (x == '<' || x == '>' || x == '=' || x == '/' || isSpace(x) || x == '"' || x == '\'') {
                                break;
                            }
                            sb.append(x);
                            i++;
                        }
                        out.add(new TextToken(sb.toString()));
                    }
                }
            }
        }

        // Restlichen Text (falls Datei nicht mit '<' endet) flushen
        flushText(text, out);
        return out;
    }

    private static boolean isSpace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '\f';
    }

    private static void flushText(StringBuilder text, List<Token> out) {
        if (!text.isEmpty()) {
            out.add(new TextToken(text.toString()));
            text.setLength(0);
        }
    }
}
