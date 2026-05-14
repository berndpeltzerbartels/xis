package one.xis.server;

public interface ClientConfigService {

    long PENDING_EVENT_TTL_SECONDS = 60;

    ClientConfig getConfig();

    void setPendingEventTtlSeconds(long seconds);
}
