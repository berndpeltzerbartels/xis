package one.xis.template;

import lombok.Data;
import one.xis.template.TemplateModel.ContentElement;
import one.xis.template.TemplateModel.Expression;
import one.xis.template.TemplateModel.StaticContent;
import one.xis.template.TemplateModel.TextContent;
import one.xis.utils.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
class MixedContentParser {
    private final String source;
    private int position = 0;

    TextContent parse() {
        List<ContentElement> contentElements = new ArrayList<>();
        while (isValidPosition(position)) {
            parseStaticContent(contentElements);
            parseVariable(contentElements);
        }
        return new TextContent(contentElements);
    }

    private void parseVariable(List<ContentElement> contentElements) {
        StringBuilder stringBuilder = new StringBuilder();
        while (isValidPosition(position)) {
            char c = charAt(position);
            if (isExpressionEnd(position++)) {
                break;
            }
            stringBuilder.append(c);
        }
        expression(stringBuilder).ifPresent(contentElements::add);
    }

    private Optional<Expression> expression(StringBuilder builder) {
        return StringUtils.isEmpty(builder) ? Optional.empty() : Optional.of(new Expression(builder.toString()));
    }

    private void parseStaticContent(List<ContentElement> contentElements) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean escaping = false;
        while (isValidPosition(position)) {
            char c = charAt(position);
            if (isEscape(c)) {
                if (!escaping) {
                    escaping = true;
                    position++;
                    continue;
                }
            }
            if (isExpressionStart(position++) && !escaping) {
                position++;
                break;
            }
            escaping = false;
            stringBuilder.append(c);
        }
        staticContent(stringBuilder).ifPresent(contentElements::add);
    }

    private Optional<StaticContent> staticContent(StringBuilder builder) {
        List<String> lines = StringUtils.splitToLines(builder)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());
        return lines.isEmpty() ? Optional.empty() : Optional.of(new StaticContent(lines));
    }

    private char charAt(int pos) {
        return source.charAt(pos);
    }

    private boolean isValidPosition(int pos) {
        return pos > -1 && pos < source.length();
    }

    private boolean isEscape(char c) {
        return c == '\\';
    }

    private boolean isExpressionStart(int pos) {
        int maxPosition = source.length() - 1;
        if (pos + 1 <= maxPosition) {
            if (charAt(pos) == '$' && charAt(pos + 1) == '{') {
                return true;
            }
        }
        return false;
    }

    private boolean isExpressionEnd(int pos) {
        return charAt(pos) == '}';
    }
}
