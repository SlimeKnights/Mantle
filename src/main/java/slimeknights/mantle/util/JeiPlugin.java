package slimeknights.mantle.util;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.util.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.screen.MultiModuleScreen;

import javax.annotation.Nonnull;
import java.util.List;

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin {

  @Override
  public void registerGuiHandlers(IGuiHandlerRegistration registration) {
    registration.addGuiContainerHandler(MultiModuleScreen.class, new IGuiContainerHandler<MultiModuleScreen>() {
      @Override
      public List<Rectangle2d> getGuiExtraAreas(@Nonnull MultiModuleScreen guiContainer) {
        return guiContainer.getModuleAreas();
      }
    });
  }

  @Override
  public void onRuntimeAvailable(@Nonnull IJeiRuntime jeiRuntime) {
  }

  @Override
  public ResourceLocation getPluginUid() {
    return new ResourceLocation(Mantle.modId, "internal");
  }

  @Override
  public void registerItemSubtypes(ISubtypeRegistration subtypeRegistry) {
  }

  @Override
  public void registerIngredients(IModIngredientRegistration registry) {
  }

}
