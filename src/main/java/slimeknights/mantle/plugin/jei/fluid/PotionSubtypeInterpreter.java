package slimeknights.mantle.plugin.jei.fluid;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/** Subtype interpreter for potion buckets and potion fluids. */
public record PotionSubtypeInterpreter<T extends DataComponentHolder>() implements ISubtypeInterpreter<T> {
  @Nullable
  @Override
  public Holder<Potion> getSubtypeData(T stack, UidContext context) {
    return stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).potion().orElse(null);
  }

  @Override
  public String getLegacyStringSubtypeInfo(T stack, UidContext context) {
    Holder<Potion> potion = getSubtypeData(stack, context);
    return potion == null ? "" : Objects.requireNonNull(potion.getKey()).toString();
  }
}
