package one.xis.ws.spring;

import com.google.gson.Gson;
import lombok.NonNull;
import one.xis.ws.WSClientRequest;
import one.xis.ws.WSServerResponse;
import one.xis.ws.WSService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpringWSHandler extends TextWebSocketHandler implements SpringWSHandlerSPI {

    private final Gson gson = new Gson();
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    private WSService wsService;

    @Override
    public void setWSService(Object wsService) {
        this.wsService = (WSService) wsService;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        var emitter = new SpringWSResponseEmitter(session, gson);
        try {
            wsService.processClientRequest(message.getPayload(), emitter);
        } catch (Exception e) {
            System.err.println("Error processing WebSocket request: " + e.getMessage());
            e.printStackTrace();
            sendErrorResponse(emitter, message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        sessions.add(session);
        System.out.println("WebSocket connection established: " + session.getId());
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
        System.out.println("WebSocket connection closed: " + session.getId() + " with status: " + status);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        exception.printStackTrace();
    }

    private void sendErrorResponse(SpringWSResponseEmitter emitter, String requestJson, Exception e) {
        try {
            var request = gson.fromJson(requestJson, WSClientRequest.class);
            var errorResponse = new WSServerResponse(500);
            errorResponse.setMessageId(request.getMessageId());
            errorResponse.setBody(null);
            emitter.send(errorResponse);
        } catch (Exception parseError) {
            System.err.println("Failed to parse request for error response: " + parseError.getMessage());
        }
    }

    void sendPingToAllSessions() {
        sessions.removeIf(session -> !session.isOpen());
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new PingMessage());
            } catch (IOException e) {
                System.err.println("Failed to send ping to session " + session.getId() + ": " + e.getMessage());
            }
        }
    }
}
