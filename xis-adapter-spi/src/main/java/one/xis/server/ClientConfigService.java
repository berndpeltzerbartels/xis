package one.xis.server;

public interface ClientConfigService {
    ClientConfig getConfig();

    void setUseWebsockets(boolean useWebsockets);
}
