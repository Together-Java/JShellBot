package org.togetherjava.discord.server.rendering;

import java.util.ArrayList;
import java.util.List;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.togetherjava.discord.server.execution.JShellWrapper;

/**
 * Contains {@link Renderer}s and allows running them in series.
 */
public class RendererManager {

  private List<Renderer> rendererList;
  private Renderer catchAll;

  public RendererManager() {
    this.rendererList = new ArrayList<>();
    this.catchAll = new StringCatchallRenderer();

    addRenderer(new ExceptionRenderer());
    addRenderer(new StandardOutputRenderer());
    addRenderer(new CompilationErrorRenderer());
    addRenderer(new RejectedColorRenderer());
  }

  /**
   * Adds the given renderer to this manager.
   *
   * @param renderer the renderer to add
   */
  private void addRenderer(Renderer renderer) {
    rendererList.add(renderer);
  }

  /**
   * Renders a given result to the passed {@link EmbedBuilder}.
   *
   * @param builder the builder to render to
   * @param result the {@link org.togetherjava.discord.server.execution.JShellWrapper.JShellResult}
   *     to render
   */
  public void renderJShellResult(EmbedBuilder builder, JShellWrapper.JShellResult result) {
    RenderUtils.applyColor(Status.VALID, builder);

    renderObject(builder, result);

    for (SnippetEvent snippetEvent : result.getEvents()) {
      renderObject(builder, snippetEvent.exception());
      renderObject(builder, snippetEvent.value());
    }
  }

  /**
   * Renders an object to a builder.
   *
   * @param builder the builder to render to
   * @param object the object to render
   */
  public void renderObject(EmbedBuilder builder, Object object) {
    if (object == null) {
      return;
    }

    boolean rendered = false;
    for (Renderer renderer : rendererList) {
      if (renderer.isApplicable(object)) {
        rendered = true;
        renderer.render(object, builder);
      }
    }

    if (!rendered && catchAll.isApplicable(object)) {
      catchAll.render(object, builder);
    }
  }
}
