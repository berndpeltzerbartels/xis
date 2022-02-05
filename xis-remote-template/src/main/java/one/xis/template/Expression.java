package one.xis.template;


import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public class Expression implements TextElement {
    private final String content;
    private final List<String> vars;
    private final String function;

    private static final Pattern PATTERN_WITH_FKT = Pattern.compile("([\\w]+)\\(([\\w,\\.]+)\\)");

    public Expression(String content) {
        this.content = content;
        Matcher matcher = PATTERN_WITH_FKT.matcher(content);
        if (matcher.find()) {
            this.function = matcher.group(1);
            String argStr = matcher.group(2);
            vars = Arrays.stream(argStr.split(",")).peek(this::validate).collect(Collectors.toList());
        } else {
            this.function = null;
            this.vars = List.of(content);
        }
    }

    private void validate(String arg) {
        if (!arg.matches("[\\w+\\.]+") || arg.contains("..") || arg.startsWith(".") || arg.endsWith(".")) {
            throw new TemplateSynthaxException(arg);
        }
    }


    @Override
    public String toString() {
        return "<%=" + content + ">";
    }
}
