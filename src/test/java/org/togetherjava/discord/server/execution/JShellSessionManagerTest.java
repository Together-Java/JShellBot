package org.togetherjava.discord.server.execution;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.togetherjava.discord.server.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class JShellSessionManagerTest {

    private JShellSessionManager jShellSessionManager;
    private static Duration sessionTTL = Duration.ofSeconds(15);

    @BeforeEach
    void setUp() {
        Properties properties = new Properties();
        properties.setProperty("session.ttl", "PT15S");
        properties.setProperty("computation.allotted_time", "PT15S");
        Config config = new Config(properties);
        jShellSessionManager = new JShellSessionManager(config);
    }

    @AfterEach
    void tearDown() {
        jShellSessionManager.shutdown();
    }

    @Test
    void cachesSessions() {
        String userId = "1";
        JShellWrapper session = jShellSessionManager.getSessionOrCreate(userId);
        JShellWrapper secondCall = jShellSessionManager.getSessionOrCreate(userId);

        assertEquals(session, secondCall, "Sessions differ");
    }

    @Test
    void createsNewSessionForDifferentUser() {
        JShellWrapper session = jShellSessionManager.getSessionOrCreate("1");
        JShellWrapper secondCall = jShellSessionManager.getSessionOrCreate("2");

        assertNotEquals(session, secondCall, "Sessions are the same");
    }

    @Test
    void timesOutSessions() {
        String userId = "1";
        JShellWrapper session = jShellSessionManager.getSessionOrCreate(userId);

        jShellSessionManager.setTimeProvider(() -> LocalDateTime.now().plus(sessionTTL).plusSeconds(5));
        jShellSessionManager.purgeOld();

        assertNotEquals(session, jShellSessionManager.getSessionOrCreate(userId), "Session was not expired");

        // restore old
        jShellSessionManager.setTimeProvider(LocalDateTime::now);
    }

    @Test
    void cachesSessionsOverTime() {
        String userId = "1";
        JShellWrapper session = jShellSessionManager.getSessionOrCreate(userId);

        jShellSessionManager.setTimeProvider(() -> LocalDateTime.now().plus(sessionTTL).minusSeconds(5));
        jShellSessionManager.purgeOld();

        assertEquals(session, jShellSessionManager.getSessionOrCreate(userId), "Session was expired");

        // restore old
        jShellSessionManager.setTimeProvider(LocalDateTime::now);
    }
}