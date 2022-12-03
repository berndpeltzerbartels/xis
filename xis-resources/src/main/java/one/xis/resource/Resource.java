package one.xis.resource;

public interface Resource {

    int getLength();

    String getContent();

    long getLastModified();

    Resource EMPTY = new EmptyResource();
}
