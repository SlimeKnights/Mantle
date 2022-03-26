package slimeknights.mantle.recipe.helper;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.crafting.conditions.IConditionSerializer;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.util.JsonHelper;

import java.util.Iterator;

/** Condition that checks when a fluid tag is empty. Same as {@link net.minecraftforge.common.crafting.conditions.TagEmptyCondition} but for fluids instead of items */
public class FluidTagEmptyCondition implements ICondition {
  private static final ResourceLocation NAME = Mantle.getResource("fluid_tag_empty");
  public static final Serializer SERIALIZER = new Serializer();
  private final TagKey<Fluid> name;

  public FluidTagEmptyCondition(String domain, String name) {
    this(new ResourceLocation(domain, name));
  }

  public FluidTagEmptyCondition(ResourceLocation name) {
    this.name = TagKey.create(Registry.FLUID_REGISTRY, name);
  }

  @Override
  public ResourceLocation getID() {
    return NAME;
  }

  @Override
  public boolean test() {
    return Registry.FLUID.getTag(name).map(HolderSet.Named::iterator).filter(Iterator::hasNext).isPresent();
  }

  @Override
  public String toString()
  {
    return "fluid_tag_empty(\"" + name + "\")";
  }

  private static class Serializer implements IConditionSerializer<FluidTagEmptyCondition> {
    @Override
    public void write(JsonObject json, FluidTagEmptyCondition value) {
      json.addProperty("tag", value.name.location().toString());
    }

    @Override
    public FluidTagEmptyCondition read(JsonObject json) {
      return new FluidTagEmptyCondition(JsonHelper.getResourceLocation(json, "tag"));
    }

    @Override
    public ResourceLocation getID()
    {
      return NAME;
    }
  }
}
