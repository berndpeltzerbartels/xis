package one.xis.server;

public interface ClientConfigService {
    ClientConfig getConfig();

    void setPendingEventTtlSeconds(long seconds);
}
