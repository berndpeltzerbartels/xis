package one.xis.html.tokens;

import java.util.ArrayList;
import java.util.List;

public class HtmlTokenizer {

    public List<Token> tokenize(String source) {
        var rv = new ArrayList<Token>();
        for (var i = 0; i < source.length(); ) {
            char c1 = source.charAt(i++);
            switch (c1) {
                case '<' -> rv.add(new OpenBracketToken());
                case '>' -> rv.add(new CloseBracketToken());
                case '=' -> rv.add(new EqualsToken());
                case '/' -> rv.add(new SlashToken());
                case ' ', '\n', '\r', '\t' -> {
                }
                default -> {
                    var sb = new StringBuilder();
                    sb.append(c1);
                    while (i < source.length()) {
                        char c = source.charAt(i);
                        if (c == '<' || c == '>' || c == '=' || c == '/') {
                            break;
                        }
                        sb.append(c);
                        i++;
                    }
                    rv.add(new TextToken(sb.toString()));
                }
            }
        }
        return rv;
    }
}
