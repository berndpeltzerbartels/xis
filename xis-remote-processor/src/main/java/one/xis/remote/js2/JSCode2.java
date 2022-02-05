package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;

import static one.xis.remote.js2.JSCodeUtil.joinCodeParts;

@RequiredArgsConstructor
class JSCode2 implements JSStatement {
    private final String js;

    JSCode2(Object... code) {
        js = joinCodeParts(code);
    }


    private String asString(Object o) {
        if (o == null) {
            return "undefined";
        }
        if (o instanceof JSStatementPart) {
            return ((JSStatementPart) o).getRef();
        }
        return o.toString();
    }

}
