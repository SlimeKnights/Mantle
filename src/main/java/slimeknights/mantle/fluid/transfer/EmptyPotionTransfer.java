package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.loadable.common.IngredientLoadable;
import slimeknights.mantle.data.loadable.primitive.IntLoadable;
import slimeknights.mantle.data.loadable.record.RecordLoadable;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.helper.TagPreference;

/** Fluid transfer info that empties a fluid from a potion item, but empties water if its the water potion */
public class EmptyPotionTransfer extends EmptyFluidContainerTransfer {
  public static final ResourceLocation ID = Mantle.getResource("empty_potion");
  /** Unique loader instance */
  public static final RecordLoadable<EmptyPotionTransfer> DESERIALIZER = RecordLoadable.create(
    IngredientLoadable.DISALLOW_EMPTY.requiredField("input", t -> t.input),
    ItemOutput.Loadable.OPTIONAL_ITEM.emptyField("result", t -> t.result),
    IntLoadable.FROM_ONE.requiredField("amount", t -> t.fluid.getAmount()),
    EmptyPotionTransfer::new);

  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, int amount) {
    super(input, filled, FluidOutput.fromFluid(Fluids.WATER, amount));
  }

  /** Helper to check if a stack contains a water potion using the new data components */
  private static boolean isWaterPotion(ItemStack stack) {
    PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
    if (contents != null && contents.potion().isPresent()) {
      return contents.potion().get().value() == Potions.WATER;
    }
    return false;
  }

  @Override
  public boolean matches(ItemStack stack, FluidStack fluid) {
    // to match, must either have water in the stack, or a potion fluid
    return super.matches(stack, fluid)
      && (TagPreference.getPreference(MantleTags.Fluids.POTION).isPresent() || isWaterPotion(stack));
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    // water just returns water
    if (isWaterPotion(stack)) {
      return fluid.copy();
    }
    // if it's not water, we need a potion fluid to return anything
    // Note: In 1.21+, fluids use data components instead of NBT tags
    return TagPreference.getPreference(MantleTags.Fluids.POTION)
      .map(value -> new FluidStack(value, fluid.getAmount()))
      .orElse(FluidStack.EMPTY);
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    DESERIALIZER.serialize(this, json);
    return json;
  }
}
