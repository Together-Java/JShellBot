package org.togetherjava.discord.server.rendering;

class StandardOutputRendererTest extends TruncationRendererTest {

    @Override
    Renderer getRenderer() {
        return new StandardOutputRenderer();
    }
}