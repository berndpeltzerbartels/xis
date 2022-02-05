package one.xis.template;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class StaticText implements TextElement {
    private final List<String> lines;

    StaticText(String s) {
        lines = List.of(s);
    }

    StaticText(List<String> lines) {
        this.lines = lines;
    }

    @Override
    public String toString() {
        return lines.stream().collect(Collectors.joining("\\\n"));
    }
}
