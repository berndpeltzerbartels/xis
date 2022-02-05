package one.xis.remote.js2;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static one.xis.remote.js2.JSCodeUtil.asString;
import static one.xis.remote.js2.JSCodeUtil.asStrings;

@Getter
class JSFunctionCall implements JSStatement {
    private final JSFunctionDeclaration functionDeclatation;
    private final List<String> params = new ArrayList<>();

    JSFunctionCall(JSFunctionDeclaration functionDeclatation, Object... paramters) {
        this.functionDeclatation = functionDeclatation;
        this.params.addAll(asStrings(paramters));
    }

    JSFunctionCall(JSFunctionDeclaration functionDeclatation, List<Object> paramters) {
        this(functionDeclatation, paramters.toArray());
    }

    void addParam(Object param) {
        params.add(asString(param));
    }
}
