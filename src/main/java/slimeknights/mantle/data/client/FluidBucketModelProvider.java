package slimeknights.mantle.data.client;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import slimeknights.mantle.client.model.FluidContainerModel;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Quick and dirty data provider to generate fluid bucket models */
@SuppressWarnings("SameParameterValue")
public class FluidBucketModelProvider extends GenericDataProvider {
  private final String modId;
  private final List<BucketModel> buckets = new ArrayList<>();
  public FluidBucketModelProvider(PackOutput packOutput, String modId) {
    super(packOutput, Target.RESOURCE_PACK, "models/item");
    this.modId = modId;
  }

  /** Record for bucket model data */
  private record BucketModel(ResourceLocation fluid, ResourceLocation bucket, boolean flipGas, boolean optional) {
    /** Creates the JSON for this model */
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("parent", "neoforge:item/bucket_drip");
      // using our own model as the forge one expects us to use item colors to handle tints, when we could just bake it in
      json.addProperty("loader", FluidContainerModel.ID.toString());
      json.addProperty("flip_gas", flipGas);
      if (optional) {
        JsonObject fluid = new JsonObject();
        fluid.addProperty("name", this.fluid.toString());
        fluid.addProperty("optional", true);
        json.add("fluid", fluid);
      } else {
        json.addProperty("fluid", this.fluid.toString());
      }
      return json;
    }
  }

  /** Adds a bucket to the list */
  protected void model(ResourceLocation fluid, ResourceLocation bucket, boolean flipGas, boolean optional) {
    buckets.add(new BucketModel(fluid, bucket, flipGas, optional));
  }

  /** Adds a bucket to the list. */
  protected void model(ResourceLocation fluid, boolean flipGas, boolean optional) {
    model(fluid, fluid.withSuffix("_bucket"), flipGas, optional);
  }

  /** Adds a bucket to the list from the fluid object, without requiring it to be bound. */
  protected void optionalFluid(FluidObject<?> fluid, boolean flipGas) {
    model(fluid.id(), flipGas, true);
  }

  /** Adds a bucket to the list from the fluid object, requires it to be bound. */
  protected void requiredFluid(FluidObject<?> fluid) {
    model(fluid.id(), fluid.getType().isLighterThanAir(), false);
  }

  /** Adds a bucket to the list from a bucket item. */
  protected void bucket(ResourceLocation id, BucketItem bucket) {
    model(Loadables.FLUID.getKey(bucket.content), id, bucket.content.getFluidType().isLighterThanAir(), false);
  }

  /** Adds a bucket to the list from a bucket item. */
  protected void bucket(BucketItem bucket) {
    bucket(Loadables.ITEM.getKey(bucket), bucket);
  }

  /** Adds buckets, may be overridden to avoid the registry scan. */
  protected void addBuckets() {
    for (Map.Entry<ResourceKey<Item>, Item> entry : BuiltInRegistries.ITEM.entrySet()) {
      ResourceLocation id = entry.getKey().location();
      if (id.getNamespace().equals(modId) && entry.getValue() instanceof BucketItem bucket) {
        bucket(id, bucket);
      }
    }
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    addBuckets();
    return allOf(buckets.stream().map(model -> saveJson(cache, model.bucket, model.serialize())));
  }

  @Override
  public String getName() {
    return modId + " Fluid Bucket Model Provider";
  }
}
