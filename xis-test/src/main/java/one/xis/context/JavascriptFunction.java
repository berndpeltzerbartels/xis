package one.xis.context;

public interface JavascriptFunction {

    void setBinding(String name, Object value);
    
    Object execute(Object... args);
}
