package one.xis.test.dom;

public interface ElementInternal<E extends ElementInternal<E>> {

    void appendChild(E child);

    void setInnerText(String text);

    void setAttribute(String name, String value);
}
