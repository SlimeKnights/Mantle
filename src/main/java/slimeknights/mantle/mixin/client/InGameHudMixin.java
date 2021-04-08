package slimeknights.mantle.mixin.client;

import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.ExtraHeartRenderHandler;

@Mixin(InGameHud.class)
public class InGameHudMixin {

  ExtraHeartRenderHandler heartRenderHandler = new ExtraHeartRenderHandler();

  @Inject(method = "renderStatusBars", at = @At("HEAD"), cancellable = true)
  private void onRenderHealth(MatrixStack matrices, CallbackInfo ci) {
    if(Mantle.config.renderExtraHeartsColored) {
      ci.cancel();
      heartRenderHandler.renderHealthbar(matrices);
    }
  }
}
