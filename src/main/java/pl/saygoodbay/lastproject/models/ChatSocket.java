package pl.saygoodbay.lastproject.models;


import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@EnableWebSocket
public class ChatSocket extends TextWebSocketHandler implements WebSocketConfigurer {

    private Map<WebSocketSession, String> sessionList = new HashMap<>();

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry
                .addHandler(this, "/chat")
                .setAllowedOrigins("*");
    }

    @Override
    protected void handleTextMessage(WebSocketSession sender, TextMessage message) throws Exception {
        String senderNickname = sessionList.get(sender);
        if (senderNickname.isEmpty()) {
            sessionList.replace(sender, message.getPayload());
            sender.sendMessage(new TextMessage("~Twój nick został ustawiony"));

            senderNickname = message.getPayload();
            sendMessageToAllWithoutSender(
                    sender,
                    new TextMessage( " <b>dołączył/a do chatu!</b>"),
                    senderNickname);
            return;
        }

        //Send message to all without me
        sendMessageToAllWithoutSender(sender, message, senderNickname);

    }

    private void sendMessageToAllWithoutSender(WebSocketSession sender, TextMessage message, String senderNickname) throws IOException {
        for (WebSocketSession sessionOfUser : sessionList.keySet()) {
            if (sessionOfUser != sender)
                sessionOfUser.sendMessage(new TextMessage(senderNickname + ": " + message.getPayload()));
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Ktos wszedł na serwer");
        sessionList.put(session, "");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Ktoś wyszedł z serwera");
        sessionList.remove(session);
    }
}
