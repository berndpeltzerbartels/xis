package one.xis.http;

public interface SseEmitter {

    void send(String data);

    void close();

    boolean isOpen();
}
