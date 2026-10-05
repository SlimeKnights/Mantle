package slimeknights.mantle.recipe.ingredient.fluid;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.data.loadable.Loadable;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.array.ArrayLoadable;
import slimeknights.mantle.data.loadable.field.RecordField;
import slimeknights.mantle.data.loadable.field.UnsyncedField;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.network.MantleStreamCodecs;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.mantle.util.typed.TypedMap;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Data object for ingredients wishing to match standard vanilla-style fluids and tags.
 * Not meant to be used directly in recipes. Just use a traditional {@link net.neoforged.neoforge.fluids.crafting.FluidIngredient} for more flexibility.
 */
@EqualsAndHashCode(exclude = "allFluids")
@RequiredArgsConstructor
public final class IngredientFluids implements Predicate<FluidStack> {
  /** Loadable instance for nesting inside other loadables. */
  public static final RecordLoadable<IngredientFluids> LOADABLE = RecordLoadable.create(
    FluidsField.INSTANCE,
    new UnsyncedField<>(Loadables.FLUID_TAG.nullableField("tag", i -> i.tag)),
    IngredientFluids::new);
  /** Map codec for nesting this inside another codec. */
  public static final MapCodec<IngredientFluids> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
    MantleCodecs.compactList(BuiltInRegistries.FLUID.byNameCodec()).fieldOf("fluid").forGetter(i -> i.fluids),
    MantleCodecs.FLUID_TAG.optionalFieldOf("tag").forGetter(i -> Optional.ofNullable(i.tag))
  ).apply(instance, (fluids, tag) -> new IngredientFluids(fluids, tag.orElse(null))));
  /** Stream codec for networking, syncing just fluids. */
  public static final StreamCodec<RegistryFriendlyByteBuf, IngredientFluids> STREAM_CODEC = StreamCodec.composite(
    MantleStreamCodecs.FLUID.apply(ByteBufCodecs.list()), IngredientFluids::getAllFluids,
    fluids -> new IngredientFluids(fluids, null));

  /** Fluids from JSON */
  private final List<Fluid> fluids;
  /** Tag key from JSON */
  @Nullable
  private final TagKey<Fluid> tag;
  /** Combined fluids from tag and list */
  private List<Fluid> allFluids;

  /** Creates from a list of fluids */
  public static IngredientFluids of(List<Fluid> fluids) {
    return new IngredientFluids(fluids, null);
  }

  /** Creates from a list of fluids */
  public static IngredientFluids of(Fluid... fluids) {
    return of(List.of(fluids));
  }

  /** Creates from a tag key */
  public static IngredientFluids of(TagKey<Fluid> tag) {
    return new IngredientFluids(List.of(), tag);
  }

  @Override
  public boolean test(FluidStack stack) {
    return fluids.contains(stack.getFluid()) || tag != null && stack.is(tag);
  }

  /** Gets all fluids in this ingredient. Used to construct display stacks and to sync over the network. */
  public List<Fluid> getAllFluids() {
    if (allFluids == null) {
      if (tag == null) {
        allFluids = fluids;
      } else {
        allFluids = Stream.concat(
          fluids.stream(),
          RegistryHelper.getTagValueStream(BuiltInRegistries.FLUID, tag)
        ).toList();
      }
    }
    return allFluids;
  }

  /** Custom field that syncs the fluid tag as fluids to the client */
  private enum FluidsField implements RecordField<List<Fluid>, IngredientFluids> {
    INSTANCE;

    private static final Loadable<List<Fluid>> FLUID_LIST = Loadables.FLUID.list(ArrayLoadable.COMPACT_OR_EMPTY);

    @Override
    public List<Fluid> get(JsonObject json, TypedMap context) {
      return FLUID_LIST.getOrDefault(json, "fluid", List.of(), context);
    }

    @Override
    public void serializeInto(IngredientFluids parent, JsonObject json, TypedMap context) {
      if (!parent.fluids.isEmpty()) {
        json.add("fluid", FLUID_LIST.serialize(parent.fluids));
      }
    }

    @Override
    public List<Fluid> decode(RegistryFriendlyByteBuf buffer, TypedMap context) {
      return FLUID_LIST.decode(buffer, context);
    }

    @Override
    public void encode(RegistryFriendlyByteBuf buffer, IngredientFluids parent, TypedMap context) {
      // sync both tag and fluid values to client
      FLUID_LIST.encode(buffer, parent.getAllFluids(), context);
    }
  }
}
