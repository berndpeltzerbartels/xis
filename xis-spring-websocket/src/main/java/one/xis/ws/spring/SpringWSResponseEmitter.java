package one.xis.ws.spring;

import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import one.xis.ws.WSEmitter;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@RequiredArgsConstructor
class SpringWSResponseEmitter implements WSEmitter {
    private final WebSocketSession session;
    private final Gson gson;

    @Override
    public void send(String responseJson) {
        try {
            session.sendMessage(new TextMessage(responseJson));
        } catch (IOException e) {
            throw new RuntimeException("Failed to send WebSocket message", e);
        }
    }

    @Override
    public void send(Object response) {
        send(gson.toJson(response));
    }

    void sendPing() {
        try {
            if (session.isOpen()) {
                session.sendMessage(new PingMessage());
            }
        } catch (IOException e) {
            System.err.println("Failed to send ping to session " + session.getId() + ": " + e.getMessage());
        }
    }

    boolean isOpen() {
        return session.isOpen();
    }
}
