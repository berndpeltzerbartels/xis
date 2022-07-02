package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class JSWriter {
    private final Appendable writer;

    public void write(JSScript script) {
        script.getDeclarations().forEach(this::writeDeclaration);
        script.getGlobalVars().forEach(this::writeGlobalVar);
        script.getStatements().forEach(statement -> this.writeStatement(statement, writer));
    }

    private void writeGlobalVar(JSVarAssignment varAssignment) {
        try {
            writeVarAssignmentStatement(varAssignment, writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeDeclaration(JSDeclaration declaration) {
        if (declaration instanceof JSClass) {
            try {
                writeClassDeclaration((JSClass) declaration, writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void writeClassDeclaration(JSClass jsClass, Appendable writer) throws IOException {
        writeConstructor(jsClass, writer);
        if (jsClass.getSuperClass() != null) {
            writer.append(jsClass.getClassName());
            writer.append(".prototype=new ");
            writer.append(jsClass.getSuperClass().getClassName());
            writer.append("();");
            writer.append("\n");
        }
    }

    private void writeConstructor(JSClass jsClass, Appendable writer) throws IOException {
        writer.append("function ");
        writer.append(jsClass.getClassName());
        writer.append("(){");

        jsClass.getFields().values().forEach(field -> {
            try {
                writer.append("this.");
                writer.append(field.getName());
                writer.append("=");
                writeValue(field.getValue(), writer);
                writer.append(";");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        jsClass.getOverriddenMethods().values().forEach(method -> {
            try {
                writer.append("this.");
                writer.append(method.getName());
                writer.append("=");
                writer.append("function(){");
                method.getStatements().forEach(statement -> writeStatement(statement, writer));
                writer.append("};");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        writer.append("};");

    }


    private void writeMethodDeclaration(JSMethod method, Appendable writer) throws IOException {
        if (method.getArgs() != 0) {
            throw new UnsupportedOperationException("overridden method with parameters is currently not supported");
        }
        writer.append(method.getOwner().getClassName());
        writer.append(".prototype.");
        writer.append(method.getName());
        writer.append("=function(");
        writer.append("){");
        method.getStatements().forEach(statement -> writeStatement(statement, writer));
        writer.append("};");
        writer.append("\n");
    }


    private void writeValue(JSValue value, Appendable writer) throws IOException {
        if (value instanceof JSConstant) {
            writeConstantValue((JSConstant) value, writer);
        } else if (value instanceof JSJsonValue) {
            writeJsonValue((JSJsonValue) value, writer);
        } else if (value instanceof JSFunctionCall) {
            writeFunctionCallValue((JSFunctionCall) value, writer);
        } else if (value instanceof JSArray) {
            writeArrayValue((JSArray) value, writer);
        } else if (value instanceof JSField) {
            writeFieldValue((JSField) value, writer);
        } else if (value instanceof JSVar) {
            writeVarValue((JSVar) value, writer);
        } else if (value instanceof JSMethodCall) {
            writeMethodCallValue((JSMethodCall) value, writer);
        } else if (value instanceof JSContructorCall) {
            writeConstrcutorCallValue((JSContructorCall) value, writer);
        } else if (value instanceof JSString) {
            writeStringValue((JSString) value, writer);
        } else if (value instanceof JSUndefined) {
            writeUndefinedValue(writer);
        } else {
            throw new UnsupportedOperationException("append " + value);
        }

    }

    private void writeUndefinedValue(Appendable writer) throws IOException {
        writer.append("undefined");
    }

    private void writeConstantValue(JSConstant jsConstant, Appendable writer) throws IOException {
        writer.append(jsConstant.getContent());
    }

    private void writeJsonValue(JSJsonValue value, Appendable writer) throws IOException {
        writer.append("{");
        Iterator<Map.Entry<String, JSValue>> fields = value.getFields().entrySet().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JSValue> field = fields.next();
            writer.append("'");
            writer.append(field.getKey());
            writer.append("'");
            writer.append(":");
            writeValue(field.getValue(), writer);
            if (fields.hasNext()) {
                writer.append(",");
            }
        }
        writer.append("}");
    }

    private void writeFunctionCallValue(JSFunctionCall functionCall, Appendable writer) throws IOException {
        writer.append(functionCall.getJsFunction().getName());
        writer.append("(");
        Iterator<JSValue> args = functionCall.getArgs().iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.append(",");
            }
        }
        writer.append(")");
    }

    private void writeArrayValue(JSArray array, Appendable writer) throws IOException {
        writer.append("[");
        Iterator<? extends JSValue> args = array.getElements().iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.append(",");
            }
        }
        writer.append("]");
    }

    private void writeFieldValue(JSField field, Appendable writer) throws IOException {
        writer.append("this.");
        writer.append(field.getName());
    }

    private void writeVarValue(JSVar jsVar, Appendable writer) throws IOException {
        writer.append(jsVar.getName());
    }


    private void writeMethodCallValue(JSMethodCall methodCall, Appendable writer) throws IOException {
        if (methodCall.getOwner() != null) {
            writer.append(methodCall.getOwner().getName());
        } else {
            writer.append("this");
        }
        writer.append(".");
        writer.append(methodCall.getMethod().getName());
        writer.append("(");
        Iterator<JSValue> args = Arrays.stream(methodCall.getArgs()).iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.append(",");
            }
        }
        writer.append(")");
    }


    private void writeConstrcutorCallValue(JSContructorCall contructorCall, Appendable writer) throws IOException {
        writer.append("new ");
        writer.append(contructorCall.getJsClass().getClassName());
        writer.append("(");
        writer.append(")");
    }

    private void writeStringValue(JSString string, Appendable writer) throws IOException {
        writer.append("'");
        writer.append(StringUtils.escape(string.getContent(), '\''));
        writer.append("'");
    }

    private void witeJSStringAppend(JSStringAppend statement, Appendable writer) throws IOException {
        writer.append(statement.getVariable().getName());
        writer.append("+=");
        writeValue(statement.getValue(), writer);
        writer.append(";");
    }

    private void writeVarAssignmentStatement(JSVarAssignment statement, Appendable writer) throws IOException {
        writer.append("var ");
        writer.append(statement.getJsVar().getName());
        writer.append("=");
        writeValue(statement.getValue(), writer);
        writer.append(";");
    }

    private void writeReturnStatement(JSReturn statement, Appendable writer) throws IOException {
        writer.append("return ");
        writeValue(statement.getValue(), writer);
        writer.append(";");
    }


    private void writeFunctionCallStatement(JSFunctionCall statement, Appendable writer) throws IOException {
        writer.append(statement.getJsFunction().getName());
        writer.append("(");
        Iterator<JSValue> valueIterator = statement.getArgs().iterator();
        while (valueIterator.hasNext()) {
            writeValue(valueIterator.next(), writer);
            if (valueIterator.hasNext()) {
                writer.append(",");
            }
        }
        writer.append(")");
        writer.append(";");
    }


    private void writeMethodCallStatement(JSMethodCall statement, Appendable writer) throws IOException {
        if (statement.getOwner() != null) {
            writer.append(statement.getOwner().getName());
        } else {
            writer.append("this");
        }
        writer.append(".");
        writer.append(statement.getMethod().getName());
        writer.append("(");
        Iterator<JSValue> valueIterator = Arrays.stream(statement.getArgs()).iterator();
        while (valueIterator.hasNext()) {
            writeValue(valueIterator.next(), writer);
            if (valueIterator.hasNext()) {
                writer.append(",");
            }
        }
        writer.append(")");
        writer.append(";");
    }


    private void writeStatement(JSStatement statement, Appendable writer) {
        try {
            if (statement instanceof JSStringAppend) {
                witeJSStringAppend((JSStringAppend) statement, writer);
            } else if (statement instanceof JSVarAssignment) {
                writeVarAssignmentStatement((JSVarAssignment) statement, writer);
            } else if (statement instanceof JSReturn) {
                writeReturnStatement((JSReturn) statement, writer);
            } else if (statement instanceof JSFunctionCall) {
                writeFunctionCallStatement((JSFunctionCall) statement, writer);
            } else if (statement instanceof JSMethodCall) {
                writeMethodCallStatement((JSMethodCall) statement, writer);
            } else {
                throw new UnsupportedOperationException("append " + statement);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
