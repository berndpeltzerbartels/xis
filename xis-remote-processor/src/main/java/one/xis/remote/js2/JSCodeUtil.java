package one.xis.remote.js2;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class JSCodeUtil {

    static String asJsArray(Collection<?> elements) {
        return "[" + asStreamOfStrings(elements.toArray()).collect(Collectors.joining(",")) + "]";
    }

    static String joinCodeParts(Object... parts) {
        return asStreamOfStrings(parts).collect(Collectors.joining());
    }

    static Stream<String> asStreamOfStrings(Object... parts) {
        return Arrays.stream(parts).map(JSCodeUtil::asString);
    }

    static List<String> asStrings(Object... parts) {
        return asStreamOfStrings(parts).collect(Collectors.toList());
    }

    static String asString(Object o) {
        if (o == null) {
            return "undefined";
        }
        if (o instanceof JSStatementPart) {
            return ((JSStatementPart) o).getRef();
        }
        if (o instanceof JSString) {
            return ((JSString) o).getContent();
        }
        return o.toString();
    }
}
