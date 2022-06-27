package one.xis.resource;

public interface ReloadableResourceFile extends ResourceFile {

    boolean isObsolete();

    void reload();
}
