package org.togetherjava.discord.server.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;

import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

class TruncationRendererTest {

    @Test
    void truncatesNewLines() {
        String string = StringUtils.repeat("\n", 40) + "some stuff";

      String rendered = RenderUtils.truncateAndSanitize(string, MessageEmbed.VALUE_MAX_LENGTH);

        int newLines = StringUtils.countMatches(rendered, '\n');
        assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines");
    }

    @Test
    void keepsNewlines() {
        String string = StringUtils.repeat("\n", RenderUtils.NEWLINE_MAXIMUM) + "some stuff";

      String rendered = RenderUtils.truncateAndSanitize(string, MessageEmbed.VALUE_MAX_LENGTH);

        int newLines = StringUtils.countMatches(rendered, '\n');
        assertEquals(RenderUtils.NEWLINE_MAXIMUM, newLines, "Expected 10 newlines.");
    }
}