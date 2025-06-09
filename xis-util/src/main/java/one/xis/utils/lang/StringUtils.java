package one.xis.utils.lang;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
public class StringUtils {

    private static final String ESCAPE = Character.toString('\\');

    public boolean isEmpty(CharSequence s) {
        return s == null || s.toString().trim().length() == 0;
    }

    public boolean isNotEmpty(CharSequence s) {
        return !isEmpty(s);
    }

    public Stream<String> splitToLines(CharSequence sequence) {
        if (sequence == null) {
            return Stream.empty();
        }
        return Arrays.stream(sequence.toString().split("[\\n\\r]+"));
    }

    public boolean isSeparatorsOnly(String s) {
        return s.matches("[\n\r ]+");
    }

    public String trimNullSafe(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    public String escape(@NonNull String s, char toEscape) {
        return s.replace(Character.toString(toEscape), ESCAPE + toEscape);
    }

    public String join(Collection<?> collection, String delimiter) {
        return collection.stream().map(Objects::toString).collect(Collectors.joining(delimiter));
    }

    public int getNumberOfOccurences(String src, char c) {
        int count = 0;
        for (int i = 0; i < src.length(); i++) {
            if (src.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    public String removeLastChar(@NonNull String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("can no remove last char of empty string");
        }
        return s.substring(0, s.length() - 1);
    }

    public String removeFirstChar(@NonNull String s) {
        if (s.isEmpty()) {
            throw new IllegalArgumentException("can no remove last char of empty string");
        }
        return s.substring(1);
    }

    public String toString(Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof String) {
            return (String) o;
        }
        // TODO Date etc
        return o.toString();
    }

    public static String before(String s, char c) {
        var index = s.indexOf(c);
        if (index < 0) {
            return s;
        }
        return s.substring(0, index);
    }
}
