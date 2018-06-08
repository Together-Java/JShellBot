package org.togetherjava.discord.server.rendering;

import jdk.jshell.EvalException;
import sx.blah.discord.util.EmbedBuilder;

public class ExceptionRenderer implements Renderer {

    @Override
    public boolean isApplicable(Object param) {
        return param instanceof Throwable;
    }

    @Override
    public EmbedBuilder render(Object object, EmbedBuilder builder) {
        Throwable throwable = (Throwable) object;
        builder
                .appendField("Exception type", throwable.getClass().getSimpleName(), true)
                .appendField("Message", throwable.getMessage(), false);

        if (throwable.getCause() != null) {
            renderCause(1, throwable, builder);
        }

        if (throwable instanceof EvalException) {
            EvalException exception = (EvalException) throwable;
            builder.appendField("Wraps", exception.getExceptionClassName(), true);
        }

        return builder;
    }

    private void renderCause(int index, Throwable throwable, EmbedBuilder builder) {
        builder
                .appendField("Cause " + index + " type", throwable.getClass().getSimpleName(), false)
                .appendField("Message", throwable.getMessage(), true);

        if (throwable.getCause() != null) {
            renderCause(index + 1, throwable.getCause(), builder);
        }
    }
}
