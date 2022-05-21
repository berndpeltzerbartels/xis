package one.xis.resource;

public interface Resource<T> {

    int getLenght();

    T getContent();

    String getContenType();

    long getLastModified();
}
