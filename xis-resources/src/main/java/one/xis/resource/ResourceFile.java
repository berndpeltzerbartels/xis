package one.xis.resource;

public interface ResourceFile {

    int getLength();

    String getContent();

    long getLastModified();

    ResourceFile EMPTY_FILE = new EmptyResourceFile();
}
