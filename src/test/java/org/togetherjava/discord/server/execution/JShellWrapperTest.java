package org.togetherjava.discord.server.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import jdk.jshell.Diag;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.togetherjava.discord.server.Config;
import org.togetherjava.discord.server.execution.JShellWrapper.JShellResult;

class JShellWrapperTest {

  private static JShellWrapper wrapper;

  @BeforeAll
  static void setupWrapper() {
    Properties properties = new Properties();
    properties.setProperty("sandbox.blacklist", "java.time");
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
    // 1crazy is an invalid variable name
    JShellWrapper.JShellResult result = wrapper.eval("1crazy").get(0);

    assertFalse(result.getEvents().isEmpty(), "Found no events");

    for (SnippetEvent snippetEvent : result.getEvents()) {
      List<Diag> diags = wrapper.getSnippetDiagnostics(snippetEvent.snippet())
          .collect(Collectors.toList());
      assertFalse(diags.isEmpty(), "Has no diagnostics");
      assertTrue(diags.get(0).isError(), "Diagnostic is no error");
    }
  }

  @Test
  void correctlyComputesExpression() {
    JShellWrapper.JShellResult result = wrapper.eval("1+1").get(0);

    assertEquals(result.getEvents().size(), 1, "Event count is not 1");

    SnippetEvent snippetEvent = result.getEvents().get(0);

    assertNull(snippetEvent.exception(), "An exception occurred");

    assertEquals("2", snippetEvent.value(), "Calculation was wrong");
  }

  @Test
  void savesHistory() {
    wrapper.eval("int test = 1+1;");
    JShellWrapper.JShellResult result = wrapper.eval("test").get(0);

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

  @ParameterizedTest(name = "Accessing \"{0}\" should fail")
  @ValueSource(strings = {
      "/opt",
      "~",
      "/tmp/"
  })
  void blocksFileAccess(String fileName) {
    JShellResult result = wrapper.eval("new java.io.File(\"" + fileName + "\").listFiles()").get(0);

    if (!allFailed(result)) {
      printSnippetResult(result);
    }

    assertTrue(
        allFailed(result),
        "Not all snippets were rejected when accessing a file."
    );
  }

  @Test
  void blocksNetworkIo() {
    JShellResult result = wrapper
        .eval("new java.net.URL(\"https://duckduckgo.com\").openConnection().connect()")
        .get(0);

    if (!allFailed(result)) {
      printSnippetResult(result);
    }

    assertTrue(
        allFailed(result),
        "Not all snippets were rejected when doing network I/O."
    );
  }

  @Test
  void blocksResettingSecurityManager() {
    JShellResult result = wrapper
        .eval("System.setSecurityManager(null)")
        .get(0);

    if (!allFailed(result)) {
      printSnippetResult(result);
    }

    assertTrue(
        allFailed(result),
        "Not all snippets were rejected when resetting the security manager."
    );
  }

  @Test()
  void doesNotEnterInfiniteLoopWhenRunningInvalidMethod() {
    JShellResult result = wrapper
        .eval("void beBad() {\n"
            + "try {\n"
            + "throw null;\n"
            + "catch (Throwable e) {\n"
            + "    e.printStackTrace()\n"
            + "}\n"
            + "}\n")
        .get(0);

    if (!allFailed(result)) {
      printSnippetResult(result);
    }

    assertTrue(
        allFailed(result),
        "Not all snippets were rejected when checking for a timeout."
    );
  }

  private boolean allFailed(JShellResult result) {
    return result.getEvents().stream()
        .allMatch(snippetEvent ->
            snippetEvent.status() == Status.REJECTED
                || snippetEvent.exception() != null
        );
  }

  private void printSnippetResult(JShellResult result) {
    for (SnippetEvent event : result.getEvents()) {
      System.out.println(event);
    }
  }
}