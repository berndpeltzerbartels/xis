package one.xis.resource;

public interface Resource {

    int getLength();

    String getContent();

    long getLastModified();

    String getResourcePath();

    Resource EMPTY = new EmptyResource();
}
