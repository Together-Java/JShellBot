package org.togetherjava.discord.server.execution;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jdk.jshell.Diag;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import org.togetherjava.discord.server.Config;
import org.togetherjava.discord.server.io.StringOutputStream;
import org.togetherjava.discord.server.sandbox.AgentAttacher;
import org.togetherjava.discord.server.sandbox.FilteredExecutionControlProvider;
import org.togetherjava.discord.server.sandbox.WhiteBlackList;

/**
 * A light wrapper around {@link JShell}, providing additional features.
 */
public class JShellWrapper {

  private static final int MAX_ANALYSIS_DEPTH = 40;

  private JShell jShell;
  private StringOutputStream outputStream;
  private TimeWatchdog watchdog;

  /**
   * Creates a new JShell wrapper using the given config and watchdog.
   *
   * @param config the config to gather properties from
   * @param watchdog the watchdog to schedule kill timer with
   */
  public JShellWrapper(Config config, TimeWatchdog watchdog) {
    this.watchdog = watchdog;
    this.outputStream = new StringOutputStream(Character.BYTES * 1600);

    this.jShell = buildJShell(outputStream, config);

    // Initialize JShell using the startup command
    jShell.eval(config.getStringOrDefault("java.startup-command", ""));
  }

  private JShell buildJShell(OutputStream outputStream, Config config) {
    PrintStream out = new PrintStream(outputStream, true, StandardCharsets.UTF_8);
    return JShell.builder()
        .out(out)
        .err(out)
        .remoteVMOptions(
            AgentAttacher.getCommandLineArgument(),
            "-Djava.security.policy=="
                + getClass().getResource("/jshell.policy").toExternalForm()
        )
        .executionEngine(getExecutionControlProvider(config), Map.of())
        .build();
  }

  private FilteredExecutionControlProvider getExecutionControlProvider(Config config) {
    return new FilteredExecutionControlProvider(WhiteBlackList.fromConfig(config));
  }

  /**
   * Closes the {@link JShell} session.
   *
   * @see JShell#close()
   */
  public void close() {
    jShell.close();
  }

  /**
   * Evaluates a command and returns the resulting snippet events and stdout.
   * <p>
   * May throw an exception.
   *
   * @param command the command to run
   * @return the result of running it
   */
  public List<JShellResult> eval(String command) {
    List<String> elementaryCommands = breakApart(command);

    AtomicBoolean stopEvaluation = new AtomicBoolean(false);

    Supplier<List<JShellResult>> work = () -> {
      List<JShellResult> results = new ArrayList<>();
      for (String elementaryCommand : elementaryCommands) {
        if (stopEvaluation.get()) {
          break;
        }
        results.add(evalSingle(elementaryCommand));
      }
      return results;
    };

    Runnable killer = () -> {
      stopEvaluation.set(true);
      jShell.stop();
    };

    return watchdog.runWatched(work, killer);
  }

  /**
   * Evaluates a command and returns the resulting snippet events and stdout.
   * <p>
   * May throw an exception.
   *
   * @param command the command to run
   * @return the result of running it
   */
  private JShellResult evalSingle(String command) {
    try {
      List<SnippetEvent> evaluate = evaluate(command);

      return new JShellResult(evaluate, getStandardOut());
    } finally {
      // always remove the output stream so it does not linger in case of an exception
      outputStream.reset();
    }
  }

  /**
   * Returns the diagnostics for the snippet. This includes things like compilation errors.
   *
   * @param snippet the snippet to return them for
   * @return all found diagnostics
   */
  public Stream<Diag> getSnippetDiagnostics(Snippet snippet) {
    return jShell.diagnostics(snippet);
  }

  private List<SnippetEvent> evaluate(String command) {
    return jShell.eval(command);
  }

  private List<String> breakApart(String input) {
    SourceCodeAnalysis sourceCodeAnalysis = jShell.sourceCodeAnalysis();

    CompletionInfo completionInfo = sourceCodeAnalysis.analyzeCompletion(input);

    int depthCounter = 0;

    List<String> fullCommand = new ArrayList<>();
    // source can be null if the input is malformed (e.g. with a method with a syntax error inside)
    while (!completionInfo.remaining().isEmpty() && completionInfo.source() != null) {
      depthCounter++;

      // should not be needed, but a while true loop here blocks a whole thread with a busy loop and
      // might lead to an OOM if the fullCommand list overflows
      if (depthCounter > MAX_ANALYSIS_DEPTH) {
        break;
      }

      fullCommand.add(completionInfo.source());
      completionInfo = sourceCodeAnalysis.analyzeCompletion(completionInfo.remaining());
    }

    // the final one
    if (completionInfo.source() != null) {
      fullCommand.add(completionInfo.source());
    } else if (completionInfo.remaining() != null) {
      // or the remaining if it errored
      fullCommand.add(completionInfo.remaining());
    }

    return fullCommand;
  }

  private String getStandardOut() {
    return outputStream.toString();
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
