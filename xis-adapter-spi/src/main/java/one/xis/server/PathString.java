package one.xis.server;

import lombok.Data;

import java.text.CharacterIterator;
import java.util.Map;
import java.util.Objects;

@Data
class PathString extends PathElement {
    private final String content;
    private final String type = "static";

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PathString)) {
            return false;
        }
        return Objects.equals(content, ((PathString) o).getContent());
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }


    static PathString read(CharacterIterator path) {
        StringBuilder content = new StringBuilder();
        char ch = path.current();
        while (ch != CharacterIterator.DONE) {
            if (!isLegalPathCharacter(ch)) {
                throw new IllegalStateException("illegal character '" + ch + "' in path: " + path);
            }
            if (ch == '{') {
                break;
            }
            content.append(ch);
            ch = path.next();
        }
        if (content.length() > 0) {
            return new PathString(content.toString());
        }
        return null;
    }

    private static boolean isLegalPathCharacter(char c) {
        if (c == '{' || c == '}' || c == '_' || c == '-' || c == '/' || c == '.' || c == '?' || c == '*') {
            return true;
        }
        if (Character.isAlphabetic(c)) {
            return true;
        }
        if (Character.isDigit(c)) {
            return true;
        }
        return false;

    }

    @Override
    public String normalized() {
        return content;
    }

    @Override
    String evaluate(Map<String, Object> pathVariables) {
        return content;
    }
}
