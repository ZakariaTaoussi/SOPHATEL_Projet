package com.example.backend.nats;

import io.nats.client.Connection;
import io.nats.client.Nats;
import io.nats.client.Options;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NatsConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(NatsConnectionManager.class);

    private final String natsUrl;
    private final boolean enabled;
    private Connection connection;

    public NatsConnectionManager(
            @Value("${nats.url:nats://localhost:4222}") String natsUrl,
            @Value("${nats.enabled:true}") boolean enabled) {
        this.natsUrl = natsUrl;
        this.enabled = enabled;
        log.info("NATS enabled: {}", enabled);
        log.info("NATS url: {}", natsUrl);
    }

    public synchronized Connection getConnection() {
        if (!enabled) {
            return null;
        }
        if (connection != null && connection.getStatus() == Connection.Status.CONNECTED) {
            return connection;
        }
        try {
            log.info("Connecting to NATS: {}", natsUrl);
            Options options = new Options.Builder()
                    .server(natsUrl)
                    .connectionTimeout(Duration.ofSeconds(2))
                    .maxReconnects(-1)
                    .reconnectWait(Duration.ofSeconds(2))
                    .build();
            connection = Nats.connect(options);
            log.info("Connected to NATS: {}", natsUrl);
            return connection;
        } catch (Exception exception) {
            log.error("Connexion NATS impossible sur {}. L'application continue sans NATS.", natsUrl, exception);
            connection = null;
            return null;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @PreDestroy
    public synchronized void close() throws InterruptedException {
        if (connection != null) {
            connection.close();
        }
    }
}
