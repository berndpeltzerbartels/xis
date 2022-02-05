package one.xis.remote.js2;

import one.xis.template.Expression;
import one.xis.template.ExpressionArg;
import one.xis.template.ExpressionString;
import one.xis.template.ExpressionVar;
import one.xis.utils.lang.CollectionUtils;

import java.util.List;

class JSExpressionParser {


    JSStatement parse(Expression expression, JSMethodDeclaration getValue) {
        if (expression.getFunction() != null) {
            return parse(expression.getFunction(), expression.getVars(), getValue);
        }
        return parse((ExpressionVar) CollectionUtils.first(expression.getVars()), getValue);
    }

    private JSFunctionCall parse(String functionName, List<ExpressionArg> args, JSMethodDeclaration getValue) {
        JSFunctionDeclaration functionDeclaration = new JSFunctionDeclaration(functionName);
        JSFunctionCall functionCall = new JSFunctionCall(functionDeclaration);
        for (ExpressionArg arg : args) {
            if (arg instanceof ExpressionString) {
                functionCall.addParam(new JSString(((ExpressionString) arg).getContent()));
            } else if (arg instanceof ExpressionVar) {
                functionCall.addParam(new JSMethodCall(getValue, new JSString(((ExpressionVar) arg).getVarName())));
            } else {
                throw new IllegalStateException();
            }
        }
        return functionCall;
    }


    private JSMethodCall parse(ExpressionVar expressionVar, JSMethodDeclaration getValue) {
        return new JSMethodCall(getValue, new JSString(expressionVar.getVarName()));
    }
}
