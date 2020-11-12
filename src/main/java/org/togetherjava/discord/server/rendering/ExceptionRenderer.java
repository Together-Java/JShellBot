package org.togetherjava.discord.server.rendering;

import java.util.Objects;
import jdk.jshell.EvalException;
import jdk.jshell.Snippet.Status;
import net.dv8tion.jda.api.EmbedBuilder;

/**
 * A renderer for exceptions.
 */
public class ExceptionRenderer implements Renderer {

  @Override
  public boolean isApplicable(Object param) {
    return param instanceof Throwable;
  }

  @Override
  public EmbedBuilder render(Object object, EmbedBuilder builder) {
    RenderUtils.applyColor(Status.REJECTED, builder);

    Throwable throwable = (Throwable) object;
    builder
        .addField("Exception type", throwable.getClass().getSimpleName(), true)
        .addField("Message", Objects.toString(throwable.getMessage()), false);

    if (throwable.getCause() != null) {
      renderCause(1, throwable, builder);
    }

    if (throwable instanceof EvalException) {
      EvalException exception = (EvalException) throwable;
      builder.addField("Wraps", exception.getExceptionClassName(), true);
    }

    return builder;
  }

  private void renderCause(int index, Throwable throwable, EmbedBuilder builder) {
    builder
        .addField("Cause " + index + " type", throwable.getClass().getSimpleName(), false)
        .addField("Message", throwable.getMessage(), true);

    if (throwable.getCause() != null) {
      renderCause(index + 1, throwable.getCause(), builder);
    }
  }
}
