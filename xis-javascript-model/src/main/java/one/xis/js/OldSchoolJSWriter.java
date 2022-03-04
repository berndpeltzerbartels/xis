package one.xis.js;

import java.io.PrintWriter;

public class OldSchoolJSWriter extends JSWriter {

    public OldSchoolJSWriter(PrintWriter writer) {
        super(writer);
    }

    @Override
    protected void writeClassDeclaration(JSClass jsClass, PrintWriter writer) {
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

}
