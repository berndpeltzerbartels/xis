package one.xis.remote.javascript;

import java.io.IOException;

class JSAppender {

    void appendJavascript(Appendable appendable) throws IOException {
    }

    /*
    // AST-Root
    @Override
    public void appendJavascript(Appendable appendable) throws IOException {
        for (JSVar jsVar : globalVars) {
            jsVar.appendJavascript(appendable);
        }
        for (JSFunction jsFunction : functions) {
            jsFunction.appendJavascript(appendable);
        }
        for (JSClass jsClass : classes) {
            jsClass.appendJavascript(appendable);
        }
        for (JSAssignment assignment : assignments) {
            assignment.appendJavascript(appendable);
        }
    }

    // class
       @Override
    public void appendJavascript(Appendable appendable) throws IOException {
        appendable.append("function ");
        appendable.append(className);
        appendable.append("(");
        appendable.append(constructorParameters.stream().collect(Collectors.joining(",")));
        appendable.append(") {\n");
        for (String param : constructorParameters) {
            appendable.append("this.");
            appendable.append(param);
            appendable.append(" = ");
            appendable.append(param);
            appendable.append(";\n");
        }
        appendable.append("}\n");
    }

    // function

    @Override
    public void appendJavascript(Appendable appendable) throws IOException {
        appendable.append("function");
        appendable.append(" ");
        appendable.append(name);
        appendable.append("(");
        appendable.append(parameters.stream().map(JSVar::getName).collect(Collectors.joining(",")));
        appendable.append(") {\n");
        for (JSStatement statement : statements) {
            statement.appendJavascript(appendable);
        }
        if (returnValue != null) {
            if (!statements.contains(returnValue)) {
                // TODO throw ...
            }
            appendable.append("return ");
            appendable.append(returnValue.getName());
            appendable.append(";\n");
        }
        appendable.append("}\n");
    }

    // var
    @Override
    public void appendJavascript(Appendable appendable) throws IOException {
        appendable.append("var ");
        appendable.append(name);
        appendable.append(";\n");
    }
        // assignment
    @Override
      public void appendJavascript(Appendable appendable) throws IOException {
        if (assignment.getAssignable() instanceof JSField) {
            appendable.append("this.");
        }
        appendable.append(assignment.getAssignable().getName());
        appendable.append(" = ");
        appendable.append(assignment.getExpression());
        appendable.append(";\n");
    }
     */
}
