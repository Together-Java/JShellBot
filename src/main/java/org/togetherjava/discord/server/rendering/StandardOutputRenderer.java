package org.togetherjava.discord.server.rendering;

import org.togetherjava.discord.server.execution.JShellWrapper;
import sx.blah.discord.util.EmbedBuilder;

public class StandardOutputRenderer implements Renderer {

    @Override
    public boolean isApplicable(Object param) {
        return param instanceof JShellWrapper.JShellResult;
    }

    @Override
    public EmbedBuilder render(Object object, EmbedBuilder builder) {
        JShellWrapper.JShellResult result = (JShellWrapper.JShellResult) object;
        if (result.getStdOut().isEmpty()) {
            return builder;
        }
        return builder
                .appendField(
                        "Output",
                        RenderUtils.truncateAndSanitize(result.getStdOut(), EmbedBuilder.FIELD_CONTENT_LIMIT),
                        true
                );
    }
}
