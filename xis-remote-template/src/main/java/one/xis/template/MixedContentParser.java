package one.xis.template;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
class MixedContentParser {

    private final String source;
    private int position = 0;
    private final ExpressionParser expressionParser = new ExpressionParser();

    List<MixedContent> parse() {
        List<MixedContent> contentList = new ArrayList<>();
        while (isValidPosition(position)) {
            parseStaticContent(contentList);
            parseVariable(contentList);
        }
        return contentList;
    }

    private void parseVariable(List<MixedContent> contentList) {
        StringBuilder stringBuilder = new StringBuilder();
        while (isValidPosition(position)) {
            char c = charAt(position);
            if (isExpressionEnd(position++)) {
                break;
            }
            stringBuilder.append(c);
        }
        expression(stringBuilder).ifPresent(contentList::add);
    }

    private Optional<ExpressionContent> expression(StringBuilder builder) {
        return StringUtils.isEmpty(builder) ? Optional.empty() : Optional.of(new ExpressionContent(expressionParser.parse(builder.toString())));
    }


    private void parseStaticContent(List<MixedContent> contentList) {
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
        staticContent(stringBuilder).forEach(contentList::add);
    }

    private List<StaticContent> staticContent(StringBuilder builder) {
        return StringUtils.splitToLines(builder)
                .filter(StringUtils::isNotEmpty)
                .map(StaticContent::new)
                .collect(Collectors.toList());
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
