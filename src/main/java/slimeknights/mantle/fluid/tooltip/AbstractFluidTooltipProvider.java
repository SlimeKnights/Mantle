package slimeknights.mantle.fluid.tooltip;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.GenericDataProvider;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/** Provider for fluid tooltip information */
@SuppressWarnings({"unused", "SameParameterValue"})  // API
public abstract class AbstractFluidTooltipProvider extends GenericDataProvider {
  private final Map<ResourceLocation,ResourceLocation> redirects = new HashMap<>();;
  private final Map<ResourceLocation,FluidUnitListBuilder> builders = new HashMap<>();
  private final String modId;

  public AbstractFluidTooltipProvider(PackOutput output, String modId) {
    super(output, Target.RESOURCE_PACK, FluidTooltipHandler.FOLDER, FluidTooltipHandler.GSON);
    this.modId = modId;
  }

  /** Adds all relevant fluids to the maps */
  protected abstract void addFluids();

  @Override
  public final CompletableFuture<?> run(CachedOutput cache) {
    addFluids();
    return allOf(Stream.concat(
      builders.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().build())),
      redirects.entrySet().stream().map(entry -> {
      JsonObject json = new JsonObject();
      json.addProperty("redirect", entry.getValue().toString());
      return saveJson(cache, entry.getKey(), json);
    })));
  }


  /* Helpers */

  /** Creates a ResourceLocation for the local mod */
  protected ResourceLocation id(String name) {
    return new ResourceLocation(modId, name);
  }

  /** Adds a fluid to the builder */
  protected FluidUnitListBuilder add(ResourceLocation id, @Nullable TagKey<Fluid> tag) {
    if (redirects.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a redirect");
    }
    FluidUnitListBuilder newBuilder = new FluidUnitListBuilder(tag);
    FluidUnitListBuilder original = builders.put(id, newBuilder);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already registered");
    }
    return newBuilder;
  }

  /** Adds a fluid to the builder */
  protected FluidUnitListBuilder add(String id, TagKey<Fluid> tag) {
    return add(id(id), tag);
  }

  /** Adds a fluid to the builder using the tag name as the ID */
  protected FluidUnitListBuilder add(TagKey<Fluid> tag) {
    return add(id(tag.location().getPath()), tag);
  }

  /** Adds a fluid to the builder with no tag */
  protected FluidUnitListBuilder add(ResourceLocation id) {
    return add(id, null);
  }

  /** Adds a fluid to the builder with no tag */
  protected FluidUnitListBuilder add(String id) {
    return add(id(id), null);
  }

  /** Adds a redirect from a named builder to a target */
  protected void addRedirect(ResourceLocation id, ResourceLocation target) {
    if (builders.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a unit list");
    }
    ResourceLocation original = redirects.put(id, target);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already redirecting to " + original);
    }
  }

  /** Builder for a unit list */
  @SuppressWarnings("unused")
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  protected class FluidUnitListBuilder {
    @Nullable
    private final TagKey<Fluid> tag;
    private final ImmutableList.Builder<FluidUnit> units = ImmutableList.builder();

    /** Adds a unit with a full translation key */
    public FluidUnitListBuilder addUnitRaw(String key, int amount) {
      units.add(new FluidUnit(key, amount));
      return this;
    }

    /** Adds a unit local to the current mod */
    public FluidUnitListBuilder addUnit(String key, int amount) {
      return addUnitRaw(Util.makeDescriptionId("gui", id("fluid." + key)), amount);
    }

    /** Adds a unit local to the given mod */
    public FluidUnitListBuilder addUnit(String key, String domain, int amount) {
      return addUnitRaw(Util.makeDescriptionId("gui", new ResourceLocation(domain, "fluid." + key)), amount);
    }

    /** Builds the final instance */
    private FluidUnitList build() {
      return new FluidUnitList(tag, units.build());
    }
  }
}
