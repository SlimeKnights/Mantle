package slimeknights.mantle.plugin.jei.fluid;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer.SlotLink;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.plugin.jei.NamedRecipeCategory;
import slimeknights.mantle.recipe.NamedRecipe;

import java.util.ArrayList;
import java.util.List;

/** Category for displaying fluid container transfer recipes in JEI */
public class FluidContainerTransferCategory extends NamedRecipeCategory<IDisplayFluidTransfer> {

  private static final Component TITLE = Mantle.makeComponent("jei", "fluid_transfer");
  private static final ResourceLocation SCALA_SPRITE = Mantle.getResource("jei/scala");

  private final IDrawable scala;
  public FluidContainerTransferCategory(IGuiHelper guiHelper) {
    super(MantleJEIConstants.FLUID_TRANSFER, TITLE, guiHelper.createDrawableItemLike(Items.POTION), 68, 37);
    this.scala = guiHelper.createDrawableSprite(Minecraft.getInstance().getGuiSprites().textureAtlas, SCALA_SPRITE, 16, 16);
  }

  @Override
  public void setRecipe(IRecipeLayoutBuilder builder, NamedRecipe<IDisplayFluidTransfer> holder, IFocusGroup focuses) {
    IDisplayFluidTransfer recipe = holder.recipe();
    // add slots
    IRecipeSlotBuilder inputItemSlot   = builder.addInputSlot( 2,  1).setStandardSlotBackground().addItemStacks(recipe.inputItems());
    IRecipeSlotBuilder resultItemSlot  = builder.addOutputSlot(50,  1).setStandardSlotBackground().addItemStacks(recipe.resultItems());
    IRecipeSlotBuilder inputFluidSlot  = builder.addInputSlot( 2, 20).setStandardSlotBackground()
      .addRichTooltipCallback(FluidTooltipCallback.UNITS)
      .setOverlay(scala, 0, 0).addIngredients(NeoForgeTypes.FLUID_STACK, recipe.inputFluids());
    IRecipeSlotBuilder resultFluidSlot = builder.addOutputSlot(50, 20).setStandardSlotBackground()
      .addRichTooltipCallback(FluidTooltipCallback.UNITS)
      .setOverlay(scala, 0, 0).addIngredients(NeoForgeTypes.FLUID_STACK, recipe.resultFluids());

    // add focus links between anything the same size
    List<SlotLink> links = recipe.linkedSlots();
    if (links.size() > 1) {
      List<IRecipeSlotBuilder> linked = new ArrayList<>(links.size());
      int size = links.getFirst().getSize(recipe);
      for (SlotLink link : links) {
        if (link.getSize(recipe) == size) {
          linked.add(switch (link) {
            case INPUT_ITEMS -> inputItemSlot;
            case RESULT_ITEMS -> resultItemSlot;
            case INPUT_FLUIDS -> inputFluidSlot;
            case RESULT_FLUIDS -> resultFluidSlot;
          });
        }
      }
      if (linked.size() > 1) {
        builder.createFocusLink(linked);
      }
    }
  }

  @Override
  public void createRecipeExtras(IRecipeExtrasBuilder builder, NamedRecipe<IDisplayFluidTransfer> recipe, IFocusGroup focuses) {
    builder.addRecipeArrowWidget().setPosition(23, 10);
  }
}
