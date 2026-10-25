package slimeknights.mantle.fluid;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import slimeknights.mantle.recipe.helper.FluidOutput;

public class PotionFluidType extends FluidType {
  public PotionFluidType(Properties properties) {
    super(properties);
  }

  @Override
  public String getDescriptionId(FluidStack stack) {
    return Potion.getName(stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion(), "item.minecraft.potion.effect.");
  }

  @Override
  public ItemStack getBucket(FluidStack fluidStack) {
    ItemStack itemStack = new ItemStack(fluidStack.getFluid().getBucket());
    itemStack.applyComponents(fluidStack.getComponentsPatch());
    return itemStack;
  }

  /** Creates the potion tag */
  private static CompoundTag potionTag(ResourceLocation location) {
    CompoundTag tag = new CompoundTag();
    tag.putString("Potion", location.toString());
    return tag;
  }

  /** Creates a fluid stack for the given potion */
  public static FluidStack potionFluid(Holder<Potion> potion, int size) {
    FluidStack fluid = new FluidStack(MantleFluids.potion.get(), size);
    fluid.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return fluid;
  }

  /** Creates a fluid output for the given potion */
  public static FluidOutput potionResult(Holder<Potion> potion, int size) {
    DataComponentPatch patch = DataComponentPatch.builder().set(DataComponents.POTION_CONTENTS, new PotionContents(potion)).build();
    return FluidOutput.fromTag(Tags.Fluids.POTION, size, patch);
  }

  /** Creates a potion bucket for the given potion */
  public static ItemStack potionBucket(Holder<Potion> potion) {
    ItemStack stack = new ItemStack(MantleFluids.potion);
    stack.set(DataComponents.POTION_CONTENTS, new PotionContents(potion));
    return stack;
  }
}
