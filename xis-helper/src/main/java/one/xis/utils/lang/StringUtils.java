package one.xis.utils.lang;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.stream.Stream;

@UtilityClass
@SuppressWarnings("unused")
public class StringUtils {

    private static final String ESCAPE = Character.toString('\\');

    public boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
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

    public boolean isLineBreaksOnly(String s) {
        return s.matches("[\n\r]+");
    }

    public String trimNullSafe(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

    public String escape(String s, char toEscape) {
        return s.replace(Character.toString(toEscape), ESCAPE + toEscape);
    }

}
