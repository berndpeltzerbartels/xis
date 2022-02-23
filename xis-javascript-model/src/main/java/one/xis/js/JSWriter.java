package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public abstract class JSWriter {
    private final PrintWriter writer;

    public void write(JSScript script) {
        script.getDeclarations().forEach(this::writeDeclaration);
    }

    private void writeDeclaration(JSDeclaration declaration) {
        if (declaration instanceof JSClass) {
            writeClassDeclaration((JSClass) declaration, writer);
        }
    }

    protected abstract void writeClassDeclaration(JSClass declaration, PrintWriter writer);

    protected void writeValue(JSValue value, PrintWriter writer) {
        if (value instanceof JSConstant) {
            writeConstantValue((JSConstant) value, writer);
        } else if (value instanceof JSJsonValue) {
            writeJsonValue((JSJsonValue) value, writer);
        } else if (value instanceof JSFunctionCall) {
            writeFunctionCallValue((JSFunctionCall) value, writer);
        } else if (value instanceof JSArray) {
            writeArrayValue((JSArray) value, writer);
        } else if (value instanceof JSObject) {
            writeObjectValue((JSObject) value, writer);
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
        } else {
            throw new UnsupportedOperationException("write " + value);
        }

    }

    protected void writeConstantValue(JSConstant jsConstant, PrintWriter writer) {
        writer.write(jsConstant.getContent());
    }

    protected void writeJsonValue(JSJsonValue value, PrintWriter writer) {
        writer.write("{");
        Iterator<Map.Entry<String, JSValue>> fields = value.getFields().entrySet().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JSValue> field = fields.next();
            writer.write(field.getKey());
            writer.write(":");
            writeValue(field.getValue(), writer);
            if (fields.hasNext()) {
                writer.write(",");
            }
        }
        writer.write("}");
    }

    protected void writeFunctionCallValue(JSFunctionCall functionCall, PrintWriter writer) {
        writer.write(functionCall.getJsFunction().getName());
        writer.write("(");
        Iterator<JSValue> args = functionCall.getArgs().iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(")");
    }

    protected void writeArrayValue(JSArray array, PrintWriter writer) {
        writer.write("[");
        Iterator<? extends JSValue> args = array.getElements().iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.write(",");
            }
        }
        writer.write("]");
    }

    protected void writeObjectValue(JSObject object, PrintWriter writer) {
        writer.write(object.getName());
    }

    protected void writeFieldValue(JSField field, PrintWriter writer) {
        writer.write("this.");
        writer.write(field.getName());
    }

    protected void writeVarValue(JSVar jsVar, PrintWriter writer) {
        writer.write(jsVar.getName());
    }


    protected void writeMethodCallValue(JSMethodCall methodCall, PrintWriter writer) {
        if (methodCall.getParent() instanceof JSObject) {
            JSObject object = (JSObject) methodCall.getParent();
            writer.write(object.getName());
        } else {
            writer.write("this.");
        }
        writer.write(".");
        writer.write(methodCall.getMethod().getName());
        writer.write("(");
        Iterator<JSValue> args = Arrays.stream(methodCall.getArgs()).iterator();
        while (args.hasNext()) {
            writeValue(args.next(), writer);
            if (args.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(")");
    }


    protected void writeConstrcutorCallValue(JSContructorCall contructorCall, PrintWriter writer) {
        writer.write("new ");
        writer.write(contructorCall.getJsClass().getClassName());
        writer.write("(");
        writer.write(")");
    }

    protected void writeStringValue(JSString string, PrintWriter writer) {
        writer.write("'");
        writer.write(StringUtils.escape(string.getContent(), '\''));
        writer.write("'");
    }

    protected void writeVarAssignmentStatement(JSVarAssignment statement, PrintWriter writer) {
        writer.write("var ");
        writer.write(statement.getJsVar().getName());
        writer.write("=");
        writeValue(statement.getValue(), writer);
        writer.write(";");
    }

    protected void writeReturnStatement(JSReturn statement, PrintWriter writer) {
        writer.write("return ");
        writeValue(statement.getValue(), writer);
        writer.write(";");
    }


    protected void writeFunctionCallStatement(JSFunctionCall statement, PrintWriter writer) {
        writer.write(statement.getJsFunction().getName());
        writer.write("(");
        Iterator<JSValue> valueIterator = statement.getArgs().iterator();
        while (valueIterator.hasNext()) {
            writeValue(valueIterator.next(), writer);
            if (valueIterator.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(")");
        writer.write(";");
    }


    protected void writeMethodCallStatement(JSMethodCall statement, PrintWriter writer) {
        if (statement.getParent() instanceof JSObject) {
            JSObject object = (JSObject) statement.getParent();
            writer.write(object.getName());
        } else {
            writer.write("this.");
        }
        writer.write(statement.getMethod().getName());
        writer.write("(");
        Iterator<JSValue> valueIterator = Arrays.stream(statement.getArgs()).iterator();
        while (valueIterator.hasNext()) {
            writeValue(valueIterator.next(), writer);
            if (valueIterator.hasNext()) {
                writer.write(",");
            }
        }
        writer.write(")");
        writer.write(";");
    }


    protected void writeStatement(JSStatement statement, PrintWriter writer) {
        if (statement instanceof JSVarAssignment) {
            writeVarAssignmentStatement((JSVarAssignment) statement, writer);
        } else if (statement instanceof JSReturn) {
            writeReturnStatement((JSReturn) statement, writer);
        } else if (statement instanceof JSFunctionCall) {
            writeFunctionCallStatement((JSFunctionCall) statement, writer);
        } else if (statement instanceof JSMethodCall) {
            writeMethodCallStatement((JSMethodCall) statement, writer);
        } else {
            throw new UnsupportedOperationException("write " + statement);
        }
    }
}
