package one.xis.js;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ES5JSWriter extends JSWriter {

    public ES5JSWriter(PrintWriter writer) {
        super(writer);
    }

    @Override
    protected void writeClassDeclaration(JSClass jsClass, PrintWriter writer) {
        writer.append("class ");
        writer.append(jsClass.getClassName());
        writer.append("{");
        writeConstructor(jsClass, writer);
        jsClass.getMethods().values().forEach(method -> writeMethodDeclaration(method, writer));
        writer.append("}");
    }

    private void writeConstructor(JSClass jsClass, PrintWriter writer) {
        writer.append("constructor() {");
        if (jsClass.getSuperClass() != null) {
            writer.append("super();");
            jsClass.getFields().values().forEach(field -> {
                writer.append("this.");
                writer.append(field.getName());
                writer.append("=");
                writeValue(field.getValue(), writer);
                writer.append(";");
            });
        }
        writer.append("}");
    }


    private void writeMethodDeclaration(JSMethod method, PrintWriter writer) {
        writer.write(method.getName());
        writer.write("(");
        Arrays.stream(method.getArgs()).collect(Collectors.joining(","));
        writer.write("){");

        writer.write("}");
    }

    @Override
    protected void writeFunctionDeclaration(JSFunction function, PrintWriter writer) {

    }
}
