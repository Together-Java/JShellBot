package org.togetherjava.discord.server.execution;

import jdk.jshell.Diag;
import jdk.jshell.SnippetEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.togetherjava.discord.server.Config;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JShellWrapperTest {

    private static JShellWrapper wrapper;

    @BeforeAll
    static void setupWrapper() {
        Properties properties = new Properties();
        properties.setProperty("blocked.packages", "java.time");
        Config config = new Config(properties);
        TimeWatchdog timeWatchdog = new TimeWatchdog(
                Executors.newScheduledThreadPool(1),
                Duration.ofMinutes(20)
        );
        wrapper = new JShellWrapper(config, timeWatchdog);
    }

    @AfterAll
    static void cleanup() {
        wrapper.close();
    }

    @Test
    void reportsCompileTimeError() {
        JShellWrapper.JShellResult result = wrapper.eval("crazy stuff");

        assertFalse(result.getEvents().isEmpty(), "Found no events");

        for (SnippetEvent snippetEvent : result.getEvents()) {
            List<Diag> diags = wrapper.getSnippetDiagnostics(snippetEvent.snippet()).collect(Collectors.toList());
            assertFalse(diags.isEmpty(), "Has no diagnostics");
            assertTrue(diags.get(0).isError(), "Diagnostic is no error");
        }
    }

    @Test
    void correctlyComputesExpression() {
        JShellWrapper.JShellResult result = wrapper.eval("1+1");

        assertEquals(result.getEvents().size(), 1, "Event count is not 1");

        SnippetEvent snippetEvent = result.getEvents().get(0);

        assertNull(snippetEvent.exception(), "An exception occurred");

        assertEquals("2", snippetEvent.value(), "Calculation was wrong");
    }

    @Test
    void savesHistory() {
        wrapper.eval("int test = 1+1;");
        JShellWrapper.JShellResult result = wrapper.eval("test");

        assertEquals(result.getEvents().size(), 1, "Event count is not 1");

        SnippetEvent snippetEvent = result.getEvents().get(0);

        assertNull(snippetEvent.exception(), "An exception occurred");

        assertEquals("2", snippetEvent.value(), "Calculation was wrong");
    }

    @Test
    void blocksPackage() {
        assertThrows(
                UnsupportedOperationException.class,
                () -> wrapper.eval("java.time.LocalDateTime.now()"),
                "No exception was thrown when accessing a blocked package."
        );
    }
}