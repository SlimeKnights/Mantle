package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

/** Condition that checks when a fluid tag is empty. Same as {@link net.minecraftforge.common.crafting.conditions.TagEmptyCondition} but for fluids instead of items */
@RequiredArgsConstructor
public class TagEmptyCondition<T> implements ICondition {
  private static final ResourceLocation NAME = Mantle.getResource("tag_empty");
  public static final Serializer SERIALIZER = new Serializer();
  private final TagKey<T> tag;

  public TagEmptyCondition(ResourceKey<? extends Registry<T>> registry, ResourceLocation name) {
    this(TagKey.create(registry, name));
  }

  @Override
  public ResourceLocation getID() {
    return NAME;
  }

  @Override
  public boolean test(IContext context) {
    return context.getTag(tag).isEmpty();
  }

  @Override
  public String toString()
  {
    return "tag_empty(\"" + tag + "\")";
  }

  private static class Serializer implements IConditionSerializer<TagEmptyCondition<?>> {
    @Override
    public void write(JsonObject json, TagEmptyCondition<?> value) {
      json.addProperty("registry", value.tag.registry().location().toString());
      json.addProperty("tag", value.tag.location().toString());
    }

    private <T> TagEmptyCondition<T> readGeneric(JsonObject json) {
      ResourceKey<Registry<T>> registry = ResourceKey.createRegistryKey(JsonHelper.getResourceLocation(json, "registry"));
      return new TagEmptyCondition<>(registry, JsonHelper.getResourceLocation(json, "tag"));
    }

    @Override
    public TagEmptyCondition<?> read(JsonObject json) {
      return readGeneric(json);
    }

    @Override
    public ResourceLocation getID()
    {
      return NAME;
    }
  }
}
