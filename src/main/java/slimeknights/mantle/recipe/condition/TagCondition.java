package slimeknights.mantle.recipe.condition;

import com.google.gson.JsonObject;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;

/** Common logic for {@link TagEmptyCondition} and {@link TagFilledCondition} */
@RequiredArgsConstructor
public abstract class TagCondition<T> implements ICondition {
  @Getter
  protected final TagKey<T> tag;
  @Nullable
  private Optional<Registry<T>> registry;

  /** Gets the registry */
  @Nullable
  protected Registry<T> registry(LootContext context) {
    // registry is not going to disappear within the lifetime of this object
    if (registry == null) {
      registry = context.getLevel().registryAccess().registry(tag.registry());
      if (registry.isEmpty()) {
        Mantle.logger.error("Failed to find registry for tag " + tag + " in " + getClass().getSimpleName() + ", this indicates a broken resource or datapack.");
      }
    }
    return registry.orElse(null);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(\"" + tag + "\")";
  }

  @Override
  public MapCodec<? extends ICondition> codec() {
    return MapCodec.unit(this);
  }

  /** Creates a codec for tag conditions. */
  protected static <C extends TagCondition<?>> MapCodec<C> codec(Function<TagKey<?>,C> constructor) {
    return RecordCodecBuilder.mapCodec(instance -> instance.group(
      ResourceLocation.CODEC.optionalFieldOf("registry", Registries.ITEM.location()).forGetter(condition -> condition.tag.registry().location()),
      ResourceLocation.CODEC.fieldOf("tag").forGetter(condition -> condition.tag.location())
    ).apply(instance, (registry, tag) -> constructor.apply(TagKey.create(ResourceKey.createRegistryKey(registry), tag))));
  }

  /** Serializer logic for tag keys */
  public record Serializer<C extends TagCondition<?>>(ResourceLocation getID, Function<TagKey<?>,C> constructor) {
    public void write(JsonObject json, C value) {
      TagKey<?> tag = value.getTag();
      // save some space in JSON by not setting registry if item (most common)
      if (!Registries.ITEM.equals(tag.registry())) {
        json.addProperty("registry", tag.registry().location().toString());
      }
      json.addProperty("tag", tag.location().toString());
    }

    public C read(JsonObject json) {
      return constructor.apply(TagKey.create(
        // default to item registry if registry is unset
        ResourceKey.createRegistryKey(JsonHelper.getResourceLocation(json, "registry", Registries.ITEM.location())),
        JsonHelper.getResourceLocation(json, "tag")));
    }
  }
}
