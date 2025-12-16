package one.xis.server;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class PageUrl {

    private final Pattern regexPattern;
    private List<String> variableNames;

    private static final Pattern PATH_VARIABLE_PATTERN = Pattern.compile("\\{([^/{}]+)}");

    PageUrl(String pattern) {
        this.regexPattern = toPattern(pattern);
    }

    private Pattern toPattern(String pattern) {
        StringBuilder regex = new StringBuilder();
        int lastIndex = 0;

        Matcher matcher = PATH_VARIABLE_PATTERN.matcher(pattern);
        variableNames = new ArrayList<>();

        while (matcher.find()) {
            String varName = matcher.group(1);
            variableNames.add(varName);

            regex.append(Pattern.quote(pattern.substring(lastIndex, matcher.start())));
            regex.append("([^/]+)");

            lastIndex = matcher.end();
        }

        // FIX: matcher.start() → pattern.length()
        regex.append(Pattern.quote(pattern.substring(lastIndex)));

        regex.insert(0, "^");
        regex.append("$");

        return Pattern.compile(regex.toString());
    }

    /**
     * Matches given url against the pattern.
     *
     * @param url
     * @return Optional.empty() if not matched. Optional of an empty map if matched without path variables.
     * Otherwise Optional of map with path variable names and their values.
     */
    Optional<Map<String, String>> matches(String url) {
        Matcher matcher = regexPattern.matcher(url);

        if (!matcher.matches()) {
            return Optional.empty();
        }

        if (variableNames.isEmpty()) {
            return Optional.of(Map.of());
        }

        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < variableNames.size(); i++) {
            result.put(variableNames.get(i), matcher.group(i + 1));
        }

        return Optional.of(result);
    }
}
