package one.xis.server;

import lombok.Data;

import java.text.CharacterIterator;
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

    @Override
    public String normalized() {
        return content;
    }
}
