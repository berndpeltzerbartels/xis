package one.xis.ws;

public interface WSEmitter {
    void send(String response);

    void send(Object response);

    boolean isOpen();

    void close();

    /**
     * Returns true if this emitter wraps the given underlying channel object.
     * Used to check whether a closing channel is still the active one for a client.
     */
    boolean isChannel(Object channel);
}
