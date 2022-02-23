package one.xis.js;

public class Functions {

    public static final JSFunction APPEND = new JSFunction("append", 2, 2);

    public static final JSFunction CREATE_ELEMENT = new JSFunction("createElement", 1, 2);

    public static final JSFunction CREATE_TEXT_NODE = new JSFunction("createTextNode", 0, 0);
    
    public static JSFunction getFunction(String name) {
        return null; // TODO
    }
}
