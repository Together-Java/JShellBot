package org.togetherjava.discord.server.rendering;

class StringCatchallRendererTest extends TruncationRendererTest {

    @Override
    Renderer getRenderer() {
        return new StringCatchallRenderer();
    }
}