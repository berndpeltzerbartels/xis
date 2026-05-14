package one.xis.test.dom;

public interface CommentNode extends Node {
    @SuppressWarnings("unused")
    void setNodeValue(String nodeValue);

    Object getNodeValue();

    @SuppressWarnings("unused")
    void setData(String data);

    @SuppressWarnings("unused")
    String getData();
}
