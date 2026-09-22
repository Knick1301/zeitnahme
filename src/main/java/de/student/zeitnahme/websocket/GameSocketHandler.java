package de.student.zeitnahme.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class GameSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, Set<WebSocketSession>> abonnenten = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<?, ?> payload = objectMapper.readValue(message.getPayload(), Map.class);
        Object spielIdRaw = payload.get("spielId");
        if (spielIdRaw == null) {
            return;
        }
        Long spielId = Long.valueOf(spielIdRaw.toString());
        abonnenten.computeIfAbsent(spielId, id -> new CopyOnWriteArraySet<>()).add(session);
        session.getAttributes().put("spielId", spielId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Object spielId = session.getAttributes().get("spielId");
        if (spielId != null) {
            Set<WebSocketSession> sessions = abonnenten.get(spielId);
            if (sessions != null) {
                sessions.remove(session);
            }
        }
    }

    public void broadcast(Long spielId, Object state) {
        Set<WebSocketSession> sessions = abonnenten.get(spielId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        try {
            TextMessage message = new TextMessage(objectMapper.writeValueAsString(state));
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}