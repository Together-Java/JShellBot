package org.togetherjava.discord.server.rendering;

import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.togetherjava.discord.server.execution.JShellWrapper;

/**
 * A renderer that adjusts the color depending on the status of the snippet.
 */
public class RejectedColorRenderer implements Renderer {

  @Override
  public boolean isApplicable(Object param) {
    return param instanceof JShellWrapper.JShellResult;
  }

  @Override
  public EmbedBuilder render(Object object, EmbedBuilder builder) {
    JShellWrapper.JShellResult result = (JShellWrapper.JShellResult) object;

    for (SnippetEvent snippetEvent : result.getEvents()) {
      RenderUtils.applyColor(snippetEvent.status(), builder);
      if (snippetEvent.exception() != null) {
        RenderUtils.applyColor(Status.REJECTED, builder);
        break;
      }
    }

    return builder;
  }
}
