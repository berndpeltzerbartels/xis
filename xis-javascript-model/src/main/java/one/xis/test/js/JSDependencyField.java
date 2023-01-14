package one.xis.test.js;

public class JSDependencyField extends JSField {
    public JSDependencyField(JSContext context, String name, JSVar dependency) {
        super(context, name);
        setValue(dependency);
    }
}
