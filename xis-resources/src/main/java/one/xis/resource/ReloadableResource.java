package one.xis.resource;

public interface ReloadableResource extends Resource {

    boolean isObsolete();

    void reload();
}
