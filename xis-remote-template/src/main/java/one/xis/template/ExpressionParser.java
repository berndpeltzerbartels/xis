package one.xis.template;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class ExpressionParser {

    private static final Pattern PATTERN_WITH_FKT = Pattern.compile("([\\w]+)\\(([\\w,\\.' \t]+)\\)");

    Expression parse(String content) {
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
        if (!arg.matches("'?[\\w+\\.]+'?") || arg.contains("..") || arg.startsWith(".") || arg.endsWith(".")) {
            throw new TemplateSynthaxException(arg);
        }
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
    }

    private ExpressionArg toExpressionVar(String s) {
        if (s.startsWith("'") && s.endsWith("'")) {
            return new ExpressionString(s.substring(1, s.length() - 2));
        }
        return new ExpressionVar(s);
    }

}
