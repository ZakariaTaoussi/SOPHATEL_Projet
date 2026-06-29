package com.example.backend.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NatsPublisher {

    private static final Logger log = LoggerFactory.getLogger(NatsPublisher.class);

    private final NatsConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    private final String demandeEventsSubject;

    public NatsPublisher(
            NatsConnectionManager connectionManager,
            ObjectMapper objectMapper,
            @Value("${nats.subject.demande-events:sophatel.demande.events}") String demandeEventsSubject) {
        this.connectionManager = connectionManager;
        this.objectMapper = objectMapper;
        this.demandeEventsSubject = demandeEventsSubject;
    }

    public void publishDemandeEvent(DemandeNotificationEvent event) {
        if (event == null) {
            log.warn("Publication NATS ignoree: evenement nul.");
            return;
        }
        if (!connectionManager.isEnabled()) {
            log.warn("Publication NATS ignoree: nats.enabled=false.");
            return;
        }
        Connection connection = connectionManager.getConnection();
        if (connection == null) {
            log.warn("Publication NATS ignoree: connexion indisponible.");
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(event);
            log.info("[NATS-PUBLISH] subject={} payload={}", demandeEventsSubject, json);
            byte[] payload = json.getBytes(StandardCharsets.UTF_8);
            connection.publish(demandeEventsSubject, payload);
            connection.flush(java.time.Duration.ofSeconds(2));
            log.info("[NATS-PUBLISH] success event={} demandeId={}", event.getEventType(), event.getDemandeId());
        } catch (Exception exception) {
            log.error("[NATS-PUBLISH] failed event={} demandeId={}",
                    event.getEventType(),
                    event.getDemandeId(),
                    exception);
        }
    }
}
