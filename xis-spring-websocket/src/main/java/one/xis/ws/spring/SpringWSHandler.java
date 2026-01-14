package one.xis.ws.spring;

import com.google.gson.Gson;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import one.xis.gson.GsonHolder;
import one.xis.ws.WSClientRequest;
import one.xis.ws.WSServerResponse;
import one.xis.ws.WSService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@Component
public class SpringWSHandler extends TextWebSocketHandler implements SpringWSHandlerSPI {

    private final Gson gson = GsonHolder.getGson();

    private WSService wsService;

    @Override
    public void setWSService(Object wsService) {
        this.wsService = (WSService) wsService;
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
        var emitter = new SpringWSResponseEmitter(session, gson);
        try {
            // Extract and store clientId in session attributes for cleanup
            var jsonObject = gson.fromJson(message.getPayload(), com.google.gson.JsonObject.class);
            var clientIdElement = jsonObject.get("clientId");
            if (clientIdElement != null && session.getAttributes().get("clientId") == null) {
                session.getAttributes().put("clientId", clientIdElement.getAsString());
            }

            wsService.processRequest(message.getPayload(), emitter);
        } catch (Exception e) {
            log.error("Error processing WebSocket request: {}", e.getMessage());
            sendErrorResponse(emitter, message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        log.info("websocket connection established");
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        // Get clientId from session attribute (set during first message)
        var clientId = (String) session.getAttributes().get("clientId");
        if (clientId != null) {
            wsService.unregisterSession(clientId);
        }
        log.info("WebSocket connection closed: clientId:  {}, status: {} ", clientId, status);
    }

    @Override
    public void handleTransportError(@NonNull WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error: {}", exception.getMessage());
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
        // Remove closed sessions from map
        wsService.removeClosedSessions(emitter -> {
            if (emitter instanceof SpringWSResponseEmitter springEmitter) {
                return !springEmitter.isOpen();
            }
            return false;
        });

        // Send ping to remaining open sessions
        wsService.getAllEmitters().stream()
                .filter(emitter -> emitter instanceof SpringWSResponseEmitter)
                .map(emitter -> (SpringWSResponseEmitter) emitter)
                .forEach(SpringWSResponseEmitter::sendPing);
    }
}
