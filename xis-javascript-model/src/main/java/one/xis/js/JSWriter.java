package one.xis.js;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.StringUtils;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

@RequiredArgsConstructor
public class JSWriter {
    private final PrintWriter writer;

    public void write(JSScript script) {
        script.getDeclarations().forEach(this::writeDeclaration);
        script.getStatements().forEach(statement -> this.writeStatement(statement, writer));
    }

    private void writeDeclaration(JSDeclaration declaration) {
        if (declaration instanceof JSClass) {
            writeClassDeclaration((JSClass) declaration, writer);
        }
    }

    private void writeClassDeclaration(JSClass jsClass, PrintWriter writer) {
        writeConstructor(jsClass, writer);
        if (jsClass.getSuperClass() != null) {
            writer.print(jsClass.getClassName());
            writer.print(".prototype=new ");
            writer.print(jsClass.getSuperClass().getClassName());
            writer.println("();");
        }
    }

    private void writeConstructor(JSClass jsClass, PrintWriter writer) {
        writer.print("function ");
        writer.print(jsClass.getClassName());
        writer.print("(){");

        jsClass.getFields().values().forEach(field -> {
            writer.print("this.");
            writer.print(field.getName());
            writer.print("=");
            writeValue(field.getValue(), writer);
            writer.print(";");
        });

        jsClass.getOverriddenMethods().values().forEach(method -> {
            writer.print("this.");
            writer.print(method.getName());
            writer.print("=");
            writer.print("function(){");
            method.getStatements().forEach(statement -> writeStatement(statement, writer));
            writer.print("};");
        });
        writer.print("};");

    }


    private void writeMethodDeclaration(JSMethod method, PrintWriter writer) {
        if (method.getArgs() != 0) {
            throw new UnsupportedOperationException("overridden method with parameters is currently not supported");
        }
        writer.print(method.getOwner().getClassName());
        writer.print(".prototype.");
        writer.print(method.getName());
        writer.print("=function(");
        writer.print("){");
        method.getStatements().forEach(statement -> writeStatement(statement, writer));
        writer.println("};");
    }


    private void writeValue(JSValue value, PrintWriter writer) {
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
            throw new UnsupportedOperationException("write " + value);
        }

    }

    private void writeUndefinedValue(PrintWriter writer) {
        writer.print("undefined");
    }

    private void writeConstantValue(JSConstant jsConstant, PrintWriter writer) {
        writer.write(jsConstant.getContent());
    }

    private void writeJsonValue(JSJsonValue value, PrintWriter writer) {
        writer.write("{");
        Iterator<Map.Entry<String, JSValue>> fields = value.getFields().entrySet().iterator();
        while (fields.hasNext()) {
            Map.Entry<String, JSValue> field = fields.next();
            writer.write("'");
            writer.write(field.getKey());
            writer.write("'");
            writer.write(":");
            writeValue(field.getValue(), writer);
            if (fields.hasNext()) {
                writer.write(",");
            }
        }
        writer.write("}");
    }

    private void writeFunctionCallValue(JSFunctionCall functionCall, PrintWriter writer) {
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

    private void writeArrayValue(JSArray array, PrintWriter writer) {
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

    private void writeFieldValue(JSField field, PrintWriter writer) {
        writer.write("this.");
        writer.write(field.getName());
    }

    private void writeVarValue(JSVar jsVar, PrintWriter writer) {
        writer.write(jsVar.getName());
    }


    private void writeMethodCallValue(JSMethodCall methodCall, PrintWriter writer) {
        if (methodCall.getOwner() != null) {
            writer.write(methodCall.getOwner().getName());
        } else {
            writer.write("this");
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


    private void writeConstrcutorCallValue(JSContructorCall contructorCall, PrintWriter writer) {
        writer.write("new ");
        writer.write(contructorCall.getJsClass().getClassName());
        writer.write("(");
        writer.write(")");
    }

    private void writeStringValue(JSString string, PrintWriter writer) {
        writer.write("'");
        writer.write(StringUtils.escape(string.getContent(), '\''));
        writer.write("'");
    }

    private void witeJSStringAppend(JSStringAppend statement, PrintWriter writer) {
        writer.write(statement.getVariable().getName());
        writer.write("+=");
        writeValue(statement.getValue(), writer);
        writer.write(";");
    }

    private void writeVarAssignmentStatement(JSVarAssignment statement, PrintWriter writer) {
        writer.write("var ");
        writer.write(statement.getJsVar().getName());
        writer.write("=");
        writeValue(statement.getValue(), writer);
        writer.write(";");
    }

    private void writeReturnStatement(JSReturn statement, PrintWriter writer) {
        writer.write("return ");
        writeValue(statement.getValue(), writer);
        writer.write(";");
    }


    private void writeFunctionCallStatement(JSFunctionCall statement, PrintWriter writer) {
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


    private void writeMethodCallStatement(JSMethodCall statement, PrintWriter writer) {
        if (statement.getOwner() != null) {
            writer.write(statement.getOwner().getName());
        } else {
            writer.write("this");
        }
        writer.write(".");
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


    private void writeStatement(JSStatement statement, PrintWriter writer) {
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
            throw new UnsupportedOperationException("write " + statement);
        }
    }


}
