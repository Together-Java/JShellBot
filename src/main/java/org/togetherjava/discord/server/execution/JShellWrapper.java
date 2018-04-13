package org.togetherjava.discord.server.execution;

import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.togetherjava.discord.server.Config;
import org.togetherjava.discord.server.io.StringOutputStream;
import org.togetherjava.discord.server.sandbox.FilteredExecutionControlProvider;
import org.togetherjava.discord.server.sandbox.Sandbox;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JShellWrapper {

    private static final Logger LOGGER = LogManager.getLogger(JShellWrapper.class);

    private JShell jShell;
    private StringOutputStream outputStream;
    private Sandbox sandbox;
    private Config config;

    public JShellWrapper(Config config) {
        this.config = config;
        this.outputStream = new StringOutputStream(Character.BYTES * 1600);
        this.jShell = buildJShell(outputStream);
        this.sandbox = new Sandbox();
    }

    private JShell buildJShell(OutputStream outputStream) {
        try {
            PrintStream out = new PrintStream(outputStream, true, "UTF-8");
            return JShell.builder()
                    .out(out)
                    .err(out)
                    .executionEngine(getExecutionControlProvider(), Map.of())
                    .build();
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported encoding: UTF-8. How?", e);

            return JShell.create();
        }
    }

    private FilteredExecutionControlProvider getExecutionControlProvider() {
        return new FilteredExecutionControlProvider(
                config.getCommaSeparatedList("blocked.packages"),
                config.getCommaSeparatedList("blocked.classes"),
                config.getCommaSeparatedList("blocked.methods").stream()
                        .map(s -> {
                            String[] parts = s.split("#");
                            return new ImmutablePair<>(parts[0], parts[1]);
                        })
                        .collect(Collectors.toList())
        );
    }

    /**
     * Closes the {@link JShell} session.
     *
     * @see JShell#close()
     */
    public void close() {
        jShell.close();
    }

    public JShellResult eval(String command) {
        try {
            return new JShellResult(evaluate(command), getStandardOut());
        } catch (Throwable e) {
            return new JShellResult(List.of(), e.getMessage());
        }
    }

    private List<SnippetEvent> evaluate(String command) {
        return sandbox.runInSandBox(() -> jShell.eval(command));
    }

    private String getStandardOut() {
        String string = outputStream.toString();
        outputStream.reset();

        return string;
    }

    /**
     * Wraps the result of executing JShell.
     */
    public static class JShellResult {
        private List<SnippetEvent> events;
        private String stdout;

        JShellResult(List<SnippetEvent> events, String stdout) {
            this.events = events;
            this.stdout = stdout == null ? "" : stdout;
        }

        public List<SnippetEvent> getEvents() {
            return Collections.unmodifiableList(events);
        }

        public String getStdOut() {
            return stdout;
        }
    }
}
