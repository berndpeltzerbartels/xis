package one.xis.template;


import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExpressionParser {

    private static final Pattern PATTERN_WITH_FKT = Pattern.compile("([\\w]+)\\(([^\\)]*)\\)");

    public Expression parse(String content) {
        List<ExpressionArg> functionArgs;
        Matcher matcher = PATTERN_WITH_FKT.matcher(content);
        if (matcher.find()) {
            String function = matcher.group(1);
            String argStr = matcher.group(2);
            functionArgs = Arrays.stream(argStr.split(","))
                    .map(String::trim)
                    .peek(this::validateArg)
                    .map(this::toExpressionVar).collect(Collectors.toList());
            return new Expression(content, functionArgs, function);
        } else if (content.contains("(") || content.contains(")")) {
            throw new TemplateSynthaxException(content);
        } else {
            return new Expression(content, List.of(new ExpressionVar(content)), null);
        }
    }

    private void validateArg(String arg) {
        int i = 0;
        if (arg.startsWith("'")) {
            i++;
        }
        if (arg.endsWith("'")) {
            i++;
        }
        if (i == 1) {
            throw new TemplateSynthaxException("unmatched \"'\" in " + arg);
        }
        if (arg.startsWith("'") && arg.endsWith("'")) {
            return;
        }
        if (!arg.matches("'?[\\w+\\.]+'?") || arg.contains("..") || arg.startsWith(".") || arg.endsWith(".")) {
            throw new TemplateSynthaxException(arg);
        }
    }

    private ExpressionArg toExpressionVar(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return new ExpressionString(s.substring(1, s.length() - 1).replace("\'", "'"));
        }
        if (s.matches("[\\d\\.]+")) {
            return new ExpressionConstant(s);
        }
        return new ExpressionVar(s);
    }

    private List<ExpressionArg> parseFunctionArgs(String source) {
        return null;
    }

}
