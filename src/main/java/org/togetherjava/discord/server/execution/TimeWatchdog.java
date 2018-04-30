package org.togetherjava.discord.server.execution;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Runs an action and cancels it if takes too long.
 *
 * <strong><em>This class is not thread safe and only one action may be running at a time.</em></strong>
 */
public class TimeWatchdog {

    private static final Logger LOGGER = LogManager.getLogger(TimeWatchdog.class);

    private final ScheduledExecutorService watchdogThreadPool;
    private final Duration maxTime;
    private final AtomicInteger operationCounter;

    public TimeWatchdog(ScheduledExecutorService watchdogThreadPool, Duration maxTime) {
        this.watchdogThreadPool = watchdogThreadPool;
        this.maxTime = maxTime;
        this.operationCounter = new AtomicInteger();
    }

    /**
     * Runs an operation and cancels it if it takes too long.
     *
     * @param action       the action to run
     * @param cancelAction cancels the passed action
     * @param <T>          the type of the result of the operation
     * @return the result of the operation
     */
    public <T> T runWatched(Supplier<T> action, Runnable cancelAction) {
        AtomicBoolean killed = new AtomicBoolean(false);
        int myId = operationCounter.incrementAndGet();

        watchdogThreadPool.schedule(() -> {
            // another calculation was done in the meantime.
            if (myId != operationCounter.get()) {
                return;
            }

            killed.set(true);

            cancelAction.run();
            LOGGER.debug("Killed a session (#" + myId + ")");
        }, maxTime.toMillis(), TimeUnit.MILLISECONDS);

        T result = action.get();

        if (killed.get()) {
            throw new AllottedTimeExceededException(maxTime);
        }

        return result;
    }
}
