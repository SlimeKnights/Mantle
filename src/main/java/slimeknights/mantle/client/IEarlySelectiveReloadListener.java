package slimeknights.mantle.client;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloadListener;
import net.minecraft.util.profiler.Profiler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * This interface is similar to forge's ISelectiveResourceReloadListener, except it runs during {@link net.minecraft.resource.SinglePreparationResourceReloadListener}'s prepare phase.
 * This is used mainly as models load during the prepare phase, so it ensures they are loaded soon enough.
 */
public interface IEarlySelectiveReloadListener extends ResourceReloadListener {
  @Override
  default CompletableFuture<Void> reload(Synchronizer stage, ResourceManager resourceManager, Profiler preparationsProfiler, Profiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
    return CompletableFuture.runAsync(() -> this.onResourceManagerReload(resourceManager), backgroundExecutor).thenCompose(stage::whenPrepared);
  }

  /**
   * @param resourceManager the resource manager being reloaded
   */
  void onResourceManagerReload(ResourceManager resourceManager);
}
