package one.xis.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtils {

    public boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }

    public boolean isNotEmpty(String s) {
        return !isEmpty(s);
    }

    public String trimNullSafe(String s) {
        if (s == null) {
            return null;
        }
        return s.trim();
    }

}
