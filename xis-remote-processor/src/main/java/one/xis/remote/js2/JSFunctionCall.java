package one.xis.remote.js2;

import lombok.Getter;

import java.util.List;

import static one.xis.remote.js2.JSCodeUtil.asStrings;

@Getter
class JSFunctionCall implements JSStatement2 {
    private final JSFunctionDeclaration functionDeclatation;
    private final List<String> params;

    JSFunctionCall(JSFunctionDeclaration functionDeclatation, Object... paramters) {
        this.functionDeclatation = functionDeclatation;
        this.params = asStrings(paramters);
    }

    JSFunctionCall(JSFunctionDeclaration functionDeclatation, List<Object> paramters) {
        this(functionDeclatation, paramters.toArray());
    }
}
