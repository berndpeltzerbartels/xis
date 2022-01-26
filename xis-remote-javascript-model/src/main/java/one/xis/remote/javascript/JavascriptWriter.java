package one.xis.remote.javascript;

import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
public class JavascriptWriter {
    private final Appendable appendable;
    private int indent;

    public void write(JSAst ast) {
        ast.getElements().forEach(this::writeAstElement);
    }

    private void writeAstElement(JSElement element) {
        if (element instanceof JSGlobal) {
            writeGlobal((JSGlobal) element);
        } else if (element instanceof JSFunction) {
            writeFunction((JSFunction) element);
        } else if (element instanceof JSClass) {
            writeClass((JSClass) element);
        } else {
            throw new IllegalStateException();
        }
        newLine();
        newLine();
    }

    private void newLine() {
        append("\n");
        try {
            for (int i = 0; i < indent; i++) {
                appendable.append("\t");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void append(String s) {
        try {
            appendable.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeClass(JSClass jsClass) {
        writeConstructor(jsClass);
        writeMethods(jsClass, jsClass.getMethods());
    }

    private void writeConstructor(JSClass jsClass) {
        indent = 0;
        append("function ");
        append(jsClass.getClassName());
        append("(");
        append(String.join(", ", jsClass.getConstructorParameters()));
        append(") {");

        indent = 1;
        newLine();
        for (String param : jsClass.getConstructorParameters()) {
            append("this.");
            append(param);
            append(" = ");
            append(param);
            append(";");
            newLine();
        }

        for (JSField field : jsClass.getFields()) {
            append("this.");
            append(field.getName());
            append(" = undefined;");
            newLine();
        }

        indent = 0;
        newLine();
        append("};");
        newLine();

    }

    private void writeMethods(JSClass jsClass, Collection<JSMethod> methods) {
        methods.forEach(method -> writeMethod(jsClass, method));
    }

    private void writeMethod(JSClass jsClass, JSMethod method) {
        append(jsClass.getClassName());
        append(".prototype.");
        append(method.getName());
        append(" = function(");
        append(String.join(", ", method.getParameters()));
        append(") {");
        newLine();
        indent = 1;
        writeStatements(method.getStatements());
        if (method.getReturnVar() != null) {
            newLine();
            append("return ");
            if (method.getReturnVar() instanceof JSField) {
                append("this.");
            }
            append(method.getReturnVar().getName());
            append(";");
            newLine();
        }
        indent = 0;
        newLine();
        append("};");
        newLine();
        newLine();

    }

    private void writeStatements(List<JSStatement> statements) {
        statements.forEach(this::writeStatement);
    }

    private void writeStatement(JSStatement statement) {
        newLine();
        if (statement instanceof JSAppend) {
            writeStatement((JSAppend) statement);
        } else if (statement instanceof JSAssignment) {
            writeStatement((JSAssignment) statement);
        } else if (statement instanceof JSVar) {
            writeStatement((JSVar) statement);
        } else if (statement instanceof JSIfStatement) {
            writeStatement((JSIfStatement) statement);
        } else if (statement instanceof JSForStatement) {
            writeStatement((JSForStatement) statement);
        }

    }

    private void writeStatement(JSAppend statement) {
        JSAssignable assignable = statement.getAssignable();
        if (assignable instanceof JSField) {
            append("this.");
        }
        append(assignable.getName());
        append(" += ");
        append(statement.getExpression());
        append(";");
    }

    private void writeStatement(JSAssignment statement) {
        JSAssignable assignable = statement.getAssignable();
        if (assignable instanceof JSField) {
            append("this.");
        }
        append(assignable.getName());
        append(" = ");
        append(statement.getExpression());
        append(";");
    }

    private void writeStatement(JSVar jsVar) {
        append("var ");
        append(jsVar.getName());
        append(";");
    }

    private void writeStatement(JSIfStatement ifStatement) {
        append("if (");
        append(ifStatement.getCondition());
        append(") {");
        newLine();
        indent++;
        writeStatements(ifStatement.getStatements());
        indent--;
        append("}");
    }

    private void writeStatement(JSForStatement forStatement) {
        append("for (var ");
        append(forStatement.getIndexVar());
        append(" = 0; ");
        append(forStatement.getIndexVar());
        append(" < ");
        append(forStatement.getArrayVar());
        append(".length; ");
        append(forStatement.getIndexVar());
        append("++) {");

        indent++;
        newLine();

        append(" var ");
        append(forStatement.getItemVar());
        append(" = ");
        append(forStatement.getArrayVar());
        append("[");
        append(forStatement.getIndexVar());
        append("];");
        newLine();
        writeStatements(forStatement.getStatements());
        indent--;
        newLine();
        append("}");
    }

    private void writeGlobal(JSGlobal global) {
        append("var ");
        append(global.getName());
        append(";");
    }

    private void writeFunction(JSFunction function) {
        indent = 0;
        append("function(");
        append(String.join(", ", function.getParameters()));
        append(") {");
        newLine();
        indent = 1;
        writeStatements(function.getStatements());
        if (function.getReturnVar() != null) {
            newLine();
            append("return ");
            append(function.getReturnVar().getName());
            append(";");
        }
        indent = 0;
        newLine();
        append("};");
    }

}
