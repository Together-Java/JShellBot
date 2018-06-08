package org.togetherjava.discord.server.io.input;

public class UnicodeQuoteSanitizer implements InputSanitizer {

    @Override
    public String sanitize(String input) {
        return input
                .replace("“", "\"")
                .replace("”", "\"");
    }
}
