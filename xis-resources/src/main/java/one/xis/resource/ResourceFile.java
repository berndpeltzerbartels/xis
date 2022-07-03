package one.xis.resource;

public interface ResourceFile {

    int getLenght();

    String getContent();

    long getLastModified();

    ResourceFile EMPTY_FILE = new EmptyResourceFile();
}
