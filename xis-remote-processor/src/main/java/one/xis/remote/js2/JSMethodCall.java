package one.xis.remote.js2;

import lombok.Getter;

import java.util.List;

import static one.xis.remote.js2.JSCodeUtil.asStrings;

@Getter
class JSMethodCall implements JSStatement2 {
    private final JSMethodDeclaration methodDeclaration;
    private final List<String> params;

    JSMethodCall(JSMethodDeclaration methodDeclaration, Object... paramters) {
        this.methodDeclaration = methodDeclaration;
        this.params = asStrings(paramters);
    }
}
