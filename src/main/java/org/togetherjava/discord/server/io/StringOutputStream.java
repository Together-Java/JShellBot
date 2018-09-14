package org.togetherjava.discord.server.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class StringOutputStream extends OutputStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringOutputStream.class);

    private static final int INITIAL_BUFFER_SIZE = 64;

    private final int maxSize;
    private byte[] buffer;
    private int size;

    public StringOutputStream(int maxSize) {
        this.maxSize = maxSize;

        reset();
    }

    /**
     * Resets this {@link StringOutputStream}, also discarding the buffer.
     */
    public void reset() {
        buffer = new byte[INITIAL_BUFFER_SIZE];
        size = 0;
    }

    @Override
    public void write(int b) throws IOException {
        ensureCapacity();

        if (size < buffer.length && size < maxSize) {
            buffer[size++] = (byte) b;
        }
    }

    private void ensureCapacity() {
        if (size >= buffer.length) {
            int newSize = size * 2;

            if (newSize > maxSize) {
                newSize = maxSize;
            }

            buffer = Arrays.copyOf(buffer, newSize);
        }
    }

    @Override
    public String toString() {
        if (size < 1) {
            return "";
        }

        try {
            return new String(Arrays.copyOf(buffer, size), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unknown encoding: UTF-8. How?", e);

            return "";
        }
    }
}
