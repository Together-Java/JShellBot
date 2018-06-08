package org.togetherjava.discord.server.io.input;

public interface InputSanitizer {

    /**
     * Sanizizes the input to Jshell so that errors in it might be accounted for.
     *
     * @param input the input to sanitize
     * @return the resulting input
     */
    String sanitize(String input);
}
