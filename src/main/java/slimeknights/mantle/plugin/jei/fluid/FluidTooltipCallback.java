package slimeknights.mantle.plugin.jei.fluid;

import com.mojang.datafixers.util.Either;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotRichTooltipCallback;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.neoforge.NeoForgeTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.neoforged.neoforge.fluids.FluidStack;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/** Helper for working with fluid tooltips */
@FunctionalInterface
public interface FluidTooltipCallback extends IRecipeSlotRichTooltipCallback {
  String AMOUNT_KEY = "jei.tooltip.liquid.amount";

  /** Default instance, simply replaces mb units with our unit handler. */
  FluidTooltipCallback UNITS = (fluid, recipeSlotView, tooltip) -> FluidTooltipHandler.appendMaterial(fluid, tooltip);

  /** Instance that removes the amount from fluid tooltips. */
  FluidTooltipCallback NO_AMOUNT = (fluid, recipeSlotView, tooltip) -> {};

  @Override
  default void onRichTooltip(IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
    ListIterator<Either<FormattedText,TooltipComponent>> listIterator = tooltip.getLines().listIterator();
    while (listIterator.hasNext()) {
      FormattedText line = listIterator.next().left().orElse(null);
      if (line instanceof Component component && component.getContents() instanceof TranslatableContents translatable && AMOUNT_KEY.equals(translatable.getKey())) {
        listIterator.remove();
        FluidStack fluid = recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK).orElse(FluidStack.EMPTY);
        onFluidTooltip(fluid, recipeSlotView, tooltip);
        return;
      }
    }
    // failed to find the tooltip to replace, so just append our stuff at the end
    recipeSlotView.getDisplayedIngredient(NeoForgeTypes.FLUID_STACK)
      .ifPresent(fluid -> onFluidTooltip(fluid, recipeSlotView, tooltip));
  }

  /** Adds rich information about the fluid to the tooltip. */
  default void onFluidTooltip(FluidStack fluid, IRecipeSlotView recipeSlotView, ITooltipBuilder tooltip) {
    List<Component> newTooltip = new ArrayList<>();
    onFluidTooltip(fluid, recipeSlotView, newTooltip);
    tooltip.addAll(newTooltip);
  }

  /** Adds information about the fluid to the tooltip */
  void onFluidTooltip(FluidStack fluid, IRecipeSlotView recipeSlotView, List<Component> tooltip);
}
