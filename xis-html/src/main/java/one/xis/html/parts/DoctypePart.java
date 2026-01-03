package one.xis.html.parts;

import lombok.Data;

@Data
public class DoctypePart implements Part {
    private final String name;
    private final int tokenCount;

    @Override
    public String toString() {
        return "<!DOCTYPE " + name + ">";
    }

    @Override
    public int tokenCount() {
        return tokenCount;
    }
}
