package org.togetherjava.discord.server.rendering;

import jdk.jshell.Diag;
import sx.blah.discord.util.EmbedBuilder;

import java.util.Locale;

public class CompilationErrorRenderer implements Renderer {
    @Override
    public boolean isApplicable(Object param) {
        return param instanceof Diag;
    }

    @Override
    public EmbedBuilder render(Object object, EmbedBuilder builder) {
        Diag diag = (Diag) object;
        return builder
                .appendField("Is compilation error", String.valueOf(diag.isError()), true)
                .appendField(
                        "Error message",
                        RenderUtils.truncateAndSanitize(diag.getMessage(Locale.ROOT), EmbedBuilder.FIELD_CONTENT_LIMIT),
                        false
                );
    }
}
