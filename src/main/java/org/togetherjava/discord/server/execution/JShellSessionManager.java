package org.togetherjava.discord.server.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.togetherjava.discord.server.Config;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

public class JShellSessionManager {

    private static Logger LOGGER = LogManager.getLogger(JShellSessionManager.class);

    private ConcurrentHashMap<String, SessionEntry> sessionMap;
    private Duration timeToLive;
    private Config config;

    private Thread ticker;

    public JShellSessionManager(Duration timeToLive, Config config) {
        this.timeToLive = timeToLive;
        this.config = config;

        this.sessionMap = new ConcurrentHashMap<>();

        this.ticker = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                purgeOld();

                try {
                    Thread.sleep(timeToLive.dividedBy(2).toMillis());
                } catch (InterruptedException e) {
                    LOGGER.warn("Session housekeeper was interrupted", e);
                    break;
                }
            }
        }, "JShellSessionManager housekeeper");

        this.ticker.start();
    }

    /**
     * Returns the {@link JShellWrapper} for the user or creates a new.
     *
     * @param userId the id of the user
     * @return the {@link JShellWrapper} to use
     * @throws IllegalStateException if this manager was already shutdown via {@link #shutdown()}
     */
    public JShellWrapper getSessionOrCreate(String userId) {
        if (ticker == null) {
            throw new IllegalStateException("This manager was shutdown already.");
        }
        SessionEntry sessionEntry = sessionMap.computeIfAbsent(
                userId,
                s -> new SessionEntry(new JShellWrapper(config), s)
        );

        return sessionEntry.getJShell();
    }

    /**
     * Stops all activity of this manager (running thready, etx.) and frees its resources. You will no longer be able
     * to get a {@link jdk.jshell.JShell} from this manager.
     * <p>
     * Should be called when the system is shut down.
     */
    public void shutdown() {
        // FIXME: 11.04.18 Actually call this to release resources when the bot shuts down
        ticker.interrupt();
        ticker = null;
        sessionMap.values().forEach(sessionEntry -> sessionEntry.getJShell().close());
    }

    /**
     * Purges sessions that were inactive for longer than the specified threshold.
     */
    private void purgeOld() {
        LOGGER.debug("Starting purge");
        LocalDateTime now = LocalDateTime.now();

        // A session could potentially be marked for removal, then another threads retrieves it and updates its
        // last accessed state, leading to an unnecessary deletion. This should not have any impact on the caller
        // though.
        sessionMap.values().removeIf(sessionEntry -> {
            Duration delta = Duration.between(now, sessionEntry.getLastAccess()).abs();

            boolean tooOld = delta.compareTo(timeToLive) > 0;

            if (tooOld) {
                sessionEntry.getJShell().close();

                LOGGER.debug(
                        "Removed session for '{}', difference was '{}'",
                        sessionEntry.getUserId(), delta
                );
            }

            return tooOld;
        });
    }

    private static class SessionEntry {
        private JShellWrapper jshell;
        private String userId;
        private LocalDateTime lastAccess;

        SessionEntry(JShellWrapper jshell, String userId) {
            this.jshell = jshell;
            this.userId = userId;
            this.lastAccess = LocalDateTime.now();
        }

        /**
         * Returns the {@link JShellWrapper} and sets the {@link #getLastAccess()} to now.
         *
         * @return the associated {@link JShellWrapper}
         */
        JShellWrapper getJShell() {
            lastAccess = LocalDateTime.now();
            return jshell;
        }

        LocalDateTime getLastAccess() {
            return lastAccess;
        }

        String getUserId() {
            return userId;
        }
    }
}
