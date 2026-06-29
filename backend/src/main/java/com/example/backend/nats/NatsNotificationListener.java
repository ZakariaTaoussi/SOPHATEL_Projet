package com.example.backend.nats;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NatsNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(NatsNotificationListener.class);

    private final NatsConnectionManager connectionManager;
    private final ObjectMapper objectMapper;
    private final NotificationEventHandler notificationEventHandler;
    private final String demandeEventsSubject;
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor();
    private Dispatcher dispatcher;
    private volatile boolean subscribed = false;

    public NatsNotificationListener(
            NatsConnectionManager connectionManager,
            ObjectMapper objectMapper,
            NotificationEventHandler notificationEventHandler,
            @Value("${nats.subject.demande-events:sophatel.demande.events}") String demandeEventsSubject) {
        this.connectionManager = connectionManager;
        this.objectMapper = objectMapper;
        this.notificationEventHandler = notificationEventHandler;
        this.demandeEventsSubject = demandeEventsSubject;
    }

    @PostConstruct
    public void subscribe() {
        subscribeIfPossible();
        retryExecutor.scheduleWithFixedDelay(this::subscribeIfPossible, 5, 5, TimeUnit.SECONDS);
    }

    private synchronized void subscribeIfPossible() {
        if (subscribed || !connectionManager.isEnabled()) {
            return;
        }
        Connection connection = connectionManager.getConnection();
        if (connection == null) {
            log.warn("[NATS-LISTENER] disabled or connection null. Nouvelle tentative planifiee.");
            return;
        }
        dispatcher = connection.createDispatcher(message -> {
            try {
                String payload = new String(message.getData(), StandardCharsets.UTF_8);
                log.info("[NATS-LISTENER] received payload={}", payload);
                DemandeNotificationEvent event = objectMapper.readValue(payload, DemandeNotificationEvent.class);
                log.info("[NATS-LISTENER] parsed eventType={} demandeId={}", event.getEventType(), event.getDemandeId());
                notificationEventHandler.handle(event);
            } catch (Exception exception) {
                log.error("[NATS-LISTENER] error while processing message", exception);
            }
        });
        dispatcher.subscribe(demandeEventsSubject);
        subscribed = true;
        log.info("[NATS-LISTENER] subscribed to subject={}", demandeEventsSubject);
    }

    @PreDestroy
    public void shutdown() {
        retryExecutor.shutdownNow();
    }
}
