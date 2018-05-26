package org.togetherjava.discord.server.rendering;

import jdk.jshell.Snippet;
import org.togetherjava.discord.server.execution.JShellWrapper;
import sx.blah.discord.util.EmbedBuilder;

public class RejectedColorRenderer implements Renderer {
    @Override
    public boolean isApplicable(Object param) {
        return param instanceof JShellWrapper.JShellResult;
    }

    @Override
    public EmbedBuilder render(Object object, EmbedBuilder builder) {
        JShellWrapper.JShellResult result = (JShellWrapper.JShellResult) object;

        if (result.getEvents().stream().anyMatch(e -> e.status() == Snippet.Status.REJECTED)) {
            RenderUtils.applyFailColor(builder);
        }

        return builder;
    }
}
