package one.xis.ws;

public interface WSEmitter {
    void send(String response);

    void send(Object response);
}
