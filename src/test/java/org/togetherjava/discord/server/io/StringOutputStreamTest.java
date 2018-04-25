package org.togetherjava.discord.server.io;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringOutputStreamTest {

    @Test
    void capturesOutput() throws IOException {
        String test = "hello world";
        checkToString(test, test, test.length(), Integer.MAX_VALUE);
    }

    @Test
    void truncates() throws IOException {
        checkToString("hello", "hell", 5, 4);
    }

    @Test
    void survivesBufferExpansion() throws IOException {
        final int length = 10_000;
        String test = StringUtils.repeat("A", length);

        checkToString(test, test, length, Integer.MAX_VALUE);
    }

    @Test
    void survivesBufferExpansionAndTruncates() throws IOException {
        final int length = 10_000;
        String test = StringUtils.repeat("A", length);
        String expected = StringUtils.repeat("A", 4000);

        checkToString(test, expected, length, 4_000);
    }

    private void checkToString(String input, String expected, int byteCount, int maxSize) throws IOException {
        StringOutputStream stringOutputStream = new StringOutputStream(maxSize);

        byte[] bytes = input.getBytes(StandardCharsets.US_ASCII);

        assertEquals(byteCount, bytes.length, "Somehow ASCII has changed?");

        stringOutputStream.write(bytes);

        assertEquals(expected, stringOutputStream.toString(), "Stored output differed.");
    }
}