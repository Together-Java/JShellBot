package org.togetherjava.discord.server.experimental;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;

public class JShellLab {

  private static Scanner stdin = new Scanner(System.in);
  private static PrintStream stdout = System.out;

  public static void main(String[] args) {
    stdout.println("Starting up...");
    JShell shell = JShell.create();
    List<SnippetEvent> result = shell.eval("5 % 2");
    for (SnippetEvent event : result) {
      StringBuilder sb = new StringBuilder();
      if (event.causeSnippet() == null) {
        switch (event.status()) {
          case VALID:
            sb.append("OK: ");
            break;
          default:
            sb.append("Failed: ");
        }
        if (event.previousStatus() == Snippet.Status.NONEXISTENT) {
          sb.append("addition");
        } else {
          sb.append("modification");
        }
        sb.append("\n\t");
        sb.append(event.snippet().source());
        sb.append("\nResult: ");
        stdout.println(sb);
        stdout.printf("Value is %s\n", event.value());

      }
    }
  }
}
