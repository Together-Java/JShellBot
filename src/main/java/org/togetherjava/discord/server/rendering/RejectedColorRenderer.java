package org.togetherjava.discord.server.rendering;

import jdk.jshell.Snippet;
import jdk.jshell.SnippetEvent;
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

        for (SnippetEvent snippetEvent : result.getEvents()) {
            if (snippetEvent.status() != Snippet.Status.VALID) {
                RenderUtils.applyFailColor(builder);
                break;
            }
            if (snippetEvent.exception() != null) {
                RenderUtils.applyFailColor(builder);
                break;
            }
        }

        return builder;
    }
}
