package slimeknights.mantle.plugin.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.ApiStatus.Internal;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.mantle.config.Config;
import slimeknights.mantle.fluid.MantleFluids;
import slimeknights.mantle.fluid.PotionFluidType;
import slimeknights.mantle.fluid.transfer.FluidContainerTransferManager;
import slimeknights.mantle.fluid.transfer.IDisplayFluidTransfer;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;
import slimeknights.mantle.plugin.jei.entity.EntityIngredientHelper;
import slimeknights.mantle.plugin.jei.entity.EntityIngredientRenderer;
import slimeknights.mantle.plugin.jei.fluid.FluidContainerTransferCategory;
import slimeknights.mantle.plugin.jei.fluid.PotionSubtypeInterpreter;
import slimeknights.mantle.recipe.MultiNamedRecipe;
import slimeknights.mantle.recipe.MultiRecipeHolder;
import slimeknights.mantle.recipe.crafting.retextured.ShapedRetexturedRecipe;
import slimeknights.mantle.recipe.ingredient.EntityIngredient.EntityInput;

import java.util.Collections;
import java.util.List;

/** JEI plugin for Mantle. Anything needed to interact with Mantle JEI content can be found in {@link MantleJEIConstants} or other classes in this package. */
@Internal
@JeiPlugin
public class JEIPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return Mantle.getResource("jei");
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registration) {
    registration.register(MantleJEIConstants.ENTITY_TYPE, Collections.emptyList(), new EntityIngredientHelper(), new EntityIngredientRenderer(16), EntityInput.CODEC);
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration registration) {
    if (MantleFluids.potion.isBound()) {
      registration.registerSubtypeInterpreter(MantleFluids.potion.asItem(), new PotionSubtypeInterpreter<>());
    }
  }

  @Override
  public <T> void registerFluidSubtypes(ISubtypeRegistration registration, IPlatformFluidHelper<T> platformFluidHelper) {
    if (MantleFluids.potion.isBound()) {
      registration.registerSubtypeInterpreter(NeoForgeTypes.FLUID_STACK, MantleFluids.potion.get(), new PotionSubtypeInterpreter<>());
    }
  }

  /** Expands multirecipes for the given vanilla type into the given JEI category. */
  private static <C extends RecipeInput, V extends Recipe<C>> void addMultiRecipes(IRecipeRegistration registration, HolderLookup.Provider provider, RecipeManager manager, RecipeType<V> vanillaType, mezz.jei.api.recipe.RecipeType<RecipeHolder<V>> jeiType) {
    List<RecipeHolder<V>> recipes = MultiRecipeHolder.getVanillaRecipes(provider, manager, vanillaType).toList();
    if (!recipes.isEmpty()) {
      registration.addRecipes(jeiType, recipes);
    }
  }

  @Override
  public void registerCategories(IRecipeCategoryRegistration registration) {
    // transfer tab is optional as the info is only useful if we have a transfer consumer
    if (MantleJEIConstants.isTransferTabEnabled()) {
      IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
      registration.addRecipeCategories(new FluidContainerTransferCategory(guiHelper));
    }
  }

  @Override
  public void registerRecipes(IRecipeRegistration registration) {
    Level level = Minecraft.getInstance().level;
    assert level != null;
    RegistryAccess access = level.registryAccess();
    RecipeManager manager = level.getRecipeManager();
    addMultiRecipes(registration, access, manager, RecipeType.CRAFTING, RecipeTypes.CRAFTING);
    addMultiRecipes(registration, access, manager, RecipeType.SMELTING, RecipeTypes.SMELTING);
    addMultiRecipes(registration, access, manager, RecipeType.SMOKING, RecipeTypes.SMOKING);
    addMultiRecipes(registration, access, manager, RecipeType.BLASTING, RecipeTypes.BLASTING);
    addMultiRecipes(registration, access, manager, RecipeType.CAMPFIRE_COOKING, RecipeTypes.CAMPFIRE_COOKING);
    addMultiRecipes(registration, access, manager, RecipeType.STONECUTTING, RecipeTypes.STONECUTTING);
    addMultiRecipes(registration, access, manager, RecipeType.SMITHING, RecipeTypes.SMITHING);

    // transfer tab is optional
    if (MantleJEIConstants.isTransferTabEnabled()) {
      registration.addRecipes(MantleJEIConstants.FLUID_TRANSFER, MultiNamedRecipe.getNamedRecipes(FluidContainerTransferManager.INSTANCE.getTransfers().stream(), access, IDisplayFluidTransfer.class));
      registration.addRecipes(MantleJEIConstants.FLUID_TRANSFER, FluidContainerTransferCategory.generateRecipes());
    }
  }

  @Override
  public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registry) {
    registry.getCraftingCategory().addExtension(ShapedRetexturedRecipe.class, new RetexturableRecipeExtension());
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(MultiModuleScreen.class, new MultiModuleContainerHandler());
  }

  @Override
  public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    if (MantleFluids.potion.isBound()) {
      // hide the blank potion with no data
      jeiRuntime.getJeiHelpers().getIngredientVisibility().hideIngredients(
        NeoForgeTypes.FLUID_STACK, List.of(new FluidStack(MantleFluids.potion.get(), FluidType.BUCKET_VOLUME)), List.of(UidContext.Ingredient));
      // if requested, show potions with each variant data
      if (Config.SHOW_POTION_FLUID_IN_JEI.getAsBoolean()) {
        jeiRuntime.getIngredientManager().addIngredientsAtRuntime(NeoForgeTypes.FLUID_STACK,
          BuiltInRegistries.POTION.holders()
            .filter(holder -> !holder.equals(Potions.WATER))
            .map(holder -> PotionFluidType.potionFluid(holder, FluidType.BUCKET_VOLUME))
            .toList());
      }
    }
  }

  private static class MultiModuleContainerHandler<C extends MultiModuleContainerMenu<?>> implements IGuiContainerHandler<MultiModuleScreen<C>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(MultiModuleScreen<C> guiContainer) {
      return guiContainer.getModuleAreas();
    }
  }
}
