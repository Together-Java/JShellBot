package org.togetherjava.discord.server.rendering;

import sx.blah.discord.util.EmbedBuilder;

import java.util.Objects;

public class StringCatchallRenderer implements Renderer {

    @Override
    public boolean isApplicable(Object param) {
        return !Objects.toString(param).isEmpty();
    }

    @Override
    public EmbedBuilder render(Object object, EmbedBuilder builder) {
        return builder.appendField(
                "Result",
                RenderUtils.truncateAndSanitize(Objects.toString(object), EmbedBuilder.FIELD_CONTENT_LIMIT),
                true
        );
    }
}
