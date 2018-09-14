package org.togetherjava.discord.server.rendering;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import sx.blah.discord.util.EmbedBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TruncationRendererTest {

    @Test
    void truncatesNewLines() {
        String string = StringUtils.repeat("\n", 40) + "some stuff";

        String rendered = RenderUtils.truncateAndSanitize(string, EmbedBuilder.FIELD_CONTENT_LIMIT);

        int newLines = StringUtils.countMatches(rendered, '\n');
        assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines");
    }

    @Test
    void keepsNewlines() {
        String string = StringUtils.repeat("\n", RenderUtils.NEWLINE_MAXIMUM) + "some stuff";

        String rendered = RenderUtils.truncateAndSanitize(string, EmbedBuilder.FIELD_CONTENT_LIMIT);

        int newLines = StringUtils.countMatches(rendered, '\n');
        assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines.");
    }
}