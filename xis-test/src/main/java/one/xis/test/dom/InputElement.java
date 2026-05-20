package one.xis.test.dom;

public interface InputElement extends Element {
    String getValue();

    boolean isChecked();

    void setValue(String v1);

    void setFile(String fileName, String contentType, byte[] bytes);

    void setFile(String fileName, byte[] bytes);

    // Non standard methods
    
}
