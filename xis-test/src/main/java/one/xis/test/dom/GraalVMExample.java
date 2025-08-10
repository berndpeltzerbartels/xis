package one.xis.test.dom;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;

public class GraalVMExample {


    public static void main(String[] args) {
        // Erstellt einen Kontext, der den Zugriff auf Java-Objekte erlaubt
        // Die Option "js.java-property-access" wird nicht mehr benötigt
        try (Context context = Context.newBuilder("js")
                .allowHostAccess(HostAccess.ALL)
                .build()) {

            MyJavaObject myJavaObject = new MyJavaObject();

            context.getBindings("js").putMember("myJavaObject", myJavaObject);

            context.eval("js", "myJavaObject.name = 'Test Name';");
            context.eval("js", "myJavaObject.active = true;");

            context.eval("js", "console.log('JS reads name: ' + myJavaObject.name);");
            context.eval("js", "console.log('JS reads active: ' + myJavaObject.active);");

            System.out.println("Final name in Java: " + myJavaObject.getName());
            System.out.println("Final active state in Java: " + myJavaObject.isActive());
        }
    }

}
