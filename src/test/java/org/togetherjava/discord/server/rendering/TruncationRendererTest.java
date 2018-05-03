package org.togetherjava.discord.server.rendering;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.util.EmbedBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class TruncationRendererTest {

    abstract Renderer getRenderer();

    @Test
    void truncatesNewLines() {
        String string = StringUtils.repeat("\n", 40) + "some stuff";

        EmbedBuilder embedBuilder = new EmbedBuilder();

        getRenderer().render(string, embedBuilder);

        for (EmbedObject.EmbedFieldObject field : embedBuilder.build().fields) {
            int newLines = StringUtils.countMatches(field.value, '\n');
            assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines");
        }
    }

    @Test
    void keepsNewlines() {
        String string = StringUtils.repeat("\n", RenderUtils.NEWLINE_MAXIMUM) + "some stuff";

        EmbedBuilder embedBuilder = new EmbedBuilder();

        getRenderer().render(string, embedBuilder);

        for (EmbedObject.EmbedFieldObject field : embedBuilder.build().fields) {
            int newLines = StringUtils.countMatches(field.value, '\n');
            assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines.");
        }
    }
}