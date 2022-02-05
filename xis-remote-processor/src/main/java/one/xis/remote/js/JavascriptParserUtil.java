package one.xis.remote.js;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;

import static one.xis.remote.js.JavacriptRootParser.REFRESH_CHILDREN_FKT;

@UtilityClass
class JavascriptParserUtil {

    final String REFRESH_METHOD_NAME = "rfr";
    final String GET_VALUE_METHOD_NAME = "gv";

    JSMethod createGetValueMethod(JSParameter nameParameter, JSArrayField values) {
        JSMethod getValue = new JSMethod(GET_VALUE_METHOD_NAME, nameParameter);
        JSVar rv = new JSVar("rv");
        getValue.addStatement(new JSVarDeclaration(rv, new JSArrayElement(values, nameParameter)));
        getValue.addStatement(new JSReturnStatement(rv));
        return getValue;
    }

    JSMethod createRefreshMethod(JSObject enclosing, JSParameter parentParameter, List<JSStatement> statements) {
        JSMethod refresh = new JSMethod(REFRESH_METHOD_NAME, parentParameter);
        statements.forEach(refresh::addStatement);
        refresh.addStatement(new JSFunctionCall(REFRESH_CHILDREN_FKT, enclosing));
        return refresh;
    }

    JSMethod createRefreshMethod(JSObject enclosing, JSParameter parentParameter, JSStatement... statements) {
        return createRefreshMethod(enclosing, parentParameter, Arrays.asList(statements));
    }
}
