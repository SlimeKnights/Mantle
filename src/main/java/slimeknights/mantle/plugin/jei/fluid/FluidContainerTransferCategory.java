package slimeknights.mantle.plugin.jei.fluid;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.transfer.EmptyPotionTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidCopyDataTransfer;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer.SlotLink;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.plugin.jei.NamedRecipeCategory;
import slimeknights.mantle.recipe.NamedRecipe;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.fluid.PotionFluidIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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


  /** Generates display recipes based on standard fluid transfer actions. */
  public static List<NamedRecipe<IDisplayFluidTransfer>> generateRecipes() {
    List<NamedRecipe<IDisplayFluidTransfer>> recipes = new ArrayList<>();
    // add recipes for emptying filled containers, as simple as draining the default instance for all it's worth
    for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(MantleTags.Items.FILLED_FLUID_CONTAINERS)) {
      ItemStack filled = new ItemStack(item);
      IFluidHandlerItem fluidHander = filled.getCapability(Capabilities.FluidHandler.ITEM);
      if (fluidHander != null && fluidHander.getTanks() > 0) {
        FluidStack fluid = fluidHander.drain(fluidHander.getTankCapacity(0), FluidAction.EXECUTE);
        if (!fluid.isEmpty()) {
          recipes.add(
            NamedRecipe.of(Objects.requireNonNull(item.getKey()).location().withPrefix("/"), List.of(),
            IDisplayFluidTransfer.builder()
              .inputItem(new ItemStack(item))
              .inputFluid(fluid)
              .resultItem(fluidHander.getContainer())
              .build()));
        }
      }
    }
    // add recipes for filling empty containers such as buckets, just try filling with every fluid
    for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(MantleTags.Items.EMPTY_FLUID_CONTAINERS)) {
      ItemStack empty = new ItemStack(item);
      IFluidHandlerItem fluidHander = empty.getCapability(Capabilities.FluidHandler.ITEM);
      if (fluidHander != null && fluidHander.getTanks() > 0) {
        int size = fluidHander.getTankCapacity(0);
        if (size > 0) {
          int fluidSize = BuiltInRegistries.FLUID.size();
          List<ItemStack> filled = new ArrayList<>(fluidSize);
          List<FluidStack> fluids = new ArrayList<>(fluidSize);
          for (Fluid fluid : BuiltInRegistries.FLUID) {
            if (fluid == Fluids.EMPTY || !fluid.isSource(fluid.defaultFluidState()) || fluid.is(MantleTags.Fluids.HIDE_IN_DISPLAY)) continue;
            FluidStack stack = new FluidStack(fluid, size);
            ItemStack copy = empty.copy();
            fluidHander = copy.getCapability(Capabilities.FluidHandler.ITEM);
            assert fluidHander != null;
            if (fluidHander.fill(stack, FluidAction.EXECUTE) == size) {
              filled.add(fluidHander.getContainer());
              fluids.add(stack);
            }
          }
          if (!filled.isEmpty() && !fluids.isEmpty()) {
            filled = List.copyOf(filled);
            fluids = List.copyOf(fluids);
            List<ItemStack> emptyList = List.of(empty);
            ResourceLocation baseKey = Objects.requireNonNull(item.getKey()).location().withPrefix("/");
            recipes.add(NamedRecipe.of(
              baseKey.withSuffix("/filling"), List.of(),
              IDisplayFluidTransfer.builder()
                .inputItems(emptyList)
                .inputFluids(fluids)
                .resultItems(filled)
                .linkedSlots(SlotLink.INPUT_FLUIDS, SlotLink.RESULT_ITEMS)
                .build()
            ));
            recipes.add(NamedRecipe.of(
              baseKey.withSuffix("/draining"), List.of(),
              IDisplayFluidTransfer.builder()
                .inputItems(filled)
                .resultFluids(fluids)
                .resultItems(emptyList)
                .linkedSlots(SlotLink.INPUT_ITEMS, SlotLink.RESULT_FLUIDS)
                .build()
            ));
          }
        }
      }
      // if the potion is active, add a fake recipe for filling and emptying its bucket since that is handled via bucket logic but hidden in bucket recipe
      if (MantleFluids.potion.isBound()) {
        ResourceLocation base = MantleFluids.potion.id().withPrefix("/");
        recipes.add(NamedRecipe.of(
          base.withPrefix("/draining"), List.of(),
          new EmptyPotionTransfer(Ingredient.of(MantleFluids.potion), ItemOutput.fromItem(Items.BUCKET), FluidType.BUCKET_VOLUME))
        );
        recipes.add(NamedRecipe.of(
          base.withPrefix("/filling"), List.of(),
          new FillFluidCopyDataTransfer(
            Ingredient.of(Items.BUCKET), ItemOutput.fromItem(MantleFluids.potion),
            new SizedFluidIngredient(PotionFluidIngredient.of(
              new NotHolderSet<>(BuiltInRegistries.POTION.asLookup(), HolderSet.direct(Potions.WATER)),
              BuiltInRegistries.FLUID.getOrCreateTag(Tags.Fluids.POTION)
            ), FluidType.BUCKET_VOLUME),
            List.of(DataComponents.POTION_CONTENTS)))
        );
      }
    }
    return recipes;
  }
}
