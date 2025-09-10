package one.xis.html.document;

import java.util.Set;

public class SelfClosingTags {
    private static final Set<String> SELF_CLOSING_TAGS = Set.of(
            "area", "base", "br", "col", "embed", "hr", "img",
            "input", "link", "meta", "param", "source", "track", "wbr"
    );

    public static boolean isSelfClosing(String tagName) {
        return SELF_CLOSING_TAGS.contains(tagName.toLowerCase());
    }
}
