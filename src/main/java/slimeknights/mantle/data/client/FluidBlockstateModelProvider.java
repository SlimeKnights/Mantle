package slimeknights.mantle.data.client;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.registration.object.FlowingFluidObject;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/** Quick and dirty data provider to generate blockstate files for fluids */
public class FluidBlockstateModelProvider extends GenericDataProvider {
  private final String modId;
  private final Set<ResourceLocation> blocks = new HashSet<>();
  public FluidBlockstateModelProvider(PackOutput packOutput, String modId) {
    super(packOutput, Target.RESOURCE_PACK, "blockstates");
    this.modId = modId;
  }

  /** Adds a model ID to generate */
  protected void block(ResourceLocation location) {
    blocks.add(location);
  }

  /** Adds a model ID to generate */
  protected void block(FlowingFluidObject<?> fluid) {
    block(fluid.id().withSuffix("_fluid"));
  }

  /** Adds all relevant models. Can be overridden to add more models beyond the block registry. */
  protected void addModels() {
    for (Map.Entry<ResourceKey<Block>,Block> entry : BuiltInRegistries.BLOCK.entrySet()) {
      ResourceLocation key = entry.getKey().location();
      if (!blocks.contains(key) && key.getNamespace().equals(modId) && entry.getValue() instanceof LiquidBlock) {
        block(key);
      }
    }
  }

  @Override
  public final CompletableFuture<?> run(CachedOutput cache) {
    addModels();

    // statically created JSON to reference block/fluid, which is just a dummy model
    JsonObject normal = new JsonObject();
    normal.addProperty("model", "mantle:block/fluid");
    JsonObject variants = new JsonObject();
    variants.add("", normal);
    JsonObject blockstate = new JsonObject();
    blockstate.add("variants", variants);

    return allOf(blocks.stream().map(location -> saveJson(cache, location, blockstate)));
  }

  @Override
  public String getName() {
    return modId + " Fluid Blockstate Model Provider";
  }
}
