package com.ankur.loganalyzer.service;

import com.ankur.loganalyzer.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AlertWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastAlert(AlertEvent event) {
        try {
            Map<String, Object> payload = Map.of(
                    "ruleName", event.getRule().getName(),
                    "conditionType", event.getRule().getConditionType().name(),
                    "message", event.getMessage(),
                    "triggeredAt", event.getTriggeredAt().toString()
            );
            messagingTemplate.convertAndSend("/topic/alerts", payload);
            log.debug("Alert broadcast via WebSocket: {}", event.getRule().getName());
        } catch (Exception e) {
            log.warn("WebSocket broadcast failed for alert '{}'", event.getRule().getName(), e);
        }
    }
}
