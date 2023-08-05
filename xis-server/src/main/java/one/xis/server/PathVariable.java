package one.xis.server;

import lombok.Data;

import java.text.CharacterIterator;
import java.util.Objects;
import java.util.regex.Pattern;

@Data
class PathVariable extends PathElement {

    static final Pattern PATTERN = Pattern.compile("\\{([^\\}]+\\})");
    private final String key;
    private final String type = "variable";

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PathVariable)) {
            return false;
        }
        return Objects.equals(key, ((PathVariable) o).getKey());
    }

    @Override
    public int hashCode() {
        return key != null ? key.hashCode() : 0;
    }

    static PathVariable read(CharacterIterator path) {
        var ch = path.current();
        if (ch != '{') {
            return null;
        }
        ch = path.next();
        StringBuilder varName = new StringBuilder();
        while (ch != CharacterIterator.DONE) {
            if (ch == '}') {
                path.next();
                break;
            }
            varName.append(ch);
            ch = path.next();
        }
        if (ch != '}') {
            throw new PathSynthaxException(ch, "'}' expected");
        }
        if (varName.length() > 0) {
            return new PathVariable(varName.toString());
        }
        return null;

    }

    @Override
    public String normalized() {
        return "*";
    }
}
