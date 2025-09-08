package one.xis.html.parts;

import lombok.Data;

@Data
public class DoctypePart implements Part {
    private final String name;

    @Override
    public String toString() {
        return "<!DOCTYPE " + name + ">";
    }
}
