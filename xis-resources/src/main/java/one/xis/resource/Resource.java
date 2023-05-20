package one.xis.resource;

public interface Resource {

    int getLength();

    String getContent();

    long getLastModified();

    String getResourcePath();

    boolean isObsolete();

    Resource EMPTY = new EmptyResource();
}
