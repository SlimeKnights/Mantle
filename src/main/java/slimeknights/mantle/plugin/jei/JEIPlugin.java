package slimeknights.mantle.plugin.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.mantle.inventory.MultiModuleContainerMenu;
import slimeknights.mantle.plugin.jei.entity.EntityIngredientHelper;
import slimeknights.mantle.plugin.jei.entity.EntityIngredientRenderer;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipe;

import java.util.Collections;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
  @Override
  public ResourceLocation getPluginUid() {
    return Mantle.getResource("jei");
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registration) {
    registration.register(MantleJEIConstants.ENTITY_TYPE, Collections.emptyList(), new EntityIngredientHelper(), new EntityIngredientRenderer(16));
  }

  /** Expands multirecipes for the given vanilla type into the given JEI category. */
  @SuppressWarnings("unchecked")
  private static <C extends Container, V extends Recipe<C>> void addMultiRecipes(IRecipeRegistration registration, RegistryAccess access, RecipeManager manager, RecipeType<V> vanillaType, mezz.jei.api.recipe.RecipeType<V> jeiType) {
    List<V> recipes = IMultiRecipe.getMultiRecipes(access, manager, vanillaType, (Class<V>) jeiType.getRecipeClass()).toList();
    if (!recipes.isEmpty()) {
      registration.addRecipes(jeiType, recipes);
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
  }

  @Override
  public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registry) {
    registry.getCraftingCategory().addCategoryExtension(ShapedRetexturedRecipe.class, RetexturableRecipeExtension::new);
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(MultiModuleScreen.class, new MultiModuleContainerHandler());
  }

  private static class MultiModuleContainerHandler<C extends MultiModuleContainerMenu<?>> implements IGuiContainerHandler<MultiModuleScreen<C>> {
    @Override
    public List<Rect2i> getGuiExtraAreas(MultiModuleScreen<C> guiContainer) {
      return guiContainer.getModuleAreas();
    }
  }
}
