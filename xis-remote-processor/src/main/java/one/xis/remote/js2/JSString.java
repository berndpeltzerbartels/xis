package one.xis.remote.js2;

import lombok.Getter;
import one.xis.utils.lang.StringUtils;

@Getter
class JSString {
    private final String content;

    JSString(String content) {
        this.content = "'" + StringUtils.escape(content, '\'') + "'";
    }
}
