package slimeknights.mantle.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import slimeknights.mantle.Mantle;

import java.util.Random;

import static net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.HEALTH;

@Environment(EnvType.CLIENT)
public class ExtraHeartRenderHandler {
  private static final Identifier ICON_HEARTS = new Identifier(Mantle.modId, "textures/gui/hearts.png");
  private static final Identifier ICON_ABSORB = new Identifier(Mantle.modId, "textures/gui/absorb.png");
  private static final Identifier ICON_VANILLA = DrawableHelper.GUI_ICONS_TEXTURE;

  private final MinecraftClient mc = MinecraftClient.getInstance();

  private int playerHealth = 0;
  private int lastPlayerHealth = 0;
  private long healthUpdateCounter = 0;
  private long lastSystemTime = 0;
  private final Random rand = new Random();

  private int regen;

  /**
   * Draws a texture to the screen
   * @param matrixStack  Matrix stack instance
   * @param x            X position
   * @param y            Y position
   * @param textureX     Texture X
   * @param textureY     Texture Y
   * @param width        Width to draw
   * @param height       Height to draw
   */
  private void blit(MatrixStack matrixStack, int x, int y, int textureX, int textureY, int width, int height) {
    MinecraftClient.getInstance().inGameHud.drawTexture(matrixStack, x, y, textureX, textureY, width, height);
  }

  /* HUD */

  /**
   * Event listener
   * @param event  Event instance
   */
  @SubscribeEvent(priority = EventPriority.LOW)
  public void renderHealthbar(RenderGameOverlayEvent.Pre event) {
    Entity renderViewEnity = this.mc.getCameraEntity();
    if (event.getType() != RenderGameOverlayEvent.ElementType.HEALTH || event.isCanceled()
        || !Mantle.config.renderExtraHeartsColored || !(renderViewEnity instanceof PlayerEntity)) {
      return;
    }

    // extra setup stuff from us
    int left_height = ForgeIngameGui.left_height;
    int width = this.mc.getWindow().getScaledWidth();
    int height = this.mc.getWindow().getScaledHeight();
    int updateCounter = this.mc.inGameHud.getTicks();

    // start default forge/mc rendering
    // changes are indicated by comment
    this.mc.getProfiler().push("health");
    RenderSystem.enableBlend();

    PlayerEntity player = (PlayerEntity) renderViewEnity;
    int health = MathHelper.ceil(player.getHealth());
    boolean highlight = this.healthUpdateCounter > (long) updateCounter && (this.healthUpdateCounter - (long) updateCounter) / 3L % 2L == 1L;

    if (health < this.playerHealth && player.timeUntilRegen > 0) {
      this.lastSystemTime = Util.getMeasuringTimeMs();
      this.healthUpdateCounter = (updateCounter + 20);
    }
    else if (health > this.playerHealth && player.timeUntilRegen > 0) {
      this.lastSystemTime = Util.getMeasuringTimeMs();
      this.healthUpdateCounter = (updateCounter + 10);
    }

    if (Util.getMeasuringTimeMs() - this.lastSystemTime > 1000L) {
      this.playerHealth = health;
      this.lastPlayerHealth = health;
      this.lastSystemTime = Util.getMeasuringTimeMs();
    }

    this.playerHealth = health;
    int healthLast = this.lastPlayerHealth;

    EntityAttributeInstance attrMaxHealth = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
    float healthMax = attrMaxHealth == null ? 0 : (float) attrMaxHealth.getValue();
    float absorb = MathHelper.ceil(player.getAbsorptionAmount());

    // CHANGE: simulate 10 hearts max if there's more, so vanilla only renders one row max
    healthMax = Math.min(healthMax, 20f);
    health = Math.min(health, 20);
    absorb = Math.min(absorb, 20);

    int healthRows = MathHelper.ceil((healthMax + absorb) / 2.0F / 10.0F);
    int rowHeight = Math.max(10 - (healthRows - 2), 3);

    this.rand.setSeed(updateCounter * 312871);

    int left = width / 2 - 91;
    int top = height - left_height;
    // change: these are unused below, unneeded? should these adjust the Forge variable?
    //left_height += (healthRows * rowHeight);
    //if (rowHeight != 10) left_height += 10 - rowHeight;

    this.regen = -1;
    if (player.hasStatusEffect(StatusEffects.REGENERATION)) {
      this.regen = updateCounter % 25;
    }

    assert this.mc.world != null;
    final int TOP = 9 * (this.mc.world.getLevelProperties().isHardcore() ? 5 : 0);
    final int BACKGROUND = (highlight ? 25 : 16);
    int MARGIN = 16;
    if      (player.hasStatusEffect(StatusEffects.POISON)) MARGIN += 36;
    else if (player.hasStatusEffect(StatusEffects.WITHER)) MARGIN += 72;
    float absorbRemaining = absorb;

    MatrixStack matrixStack = event.getMatrixStack();
    for (int i = MathHelper.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
      int row = MathHelper.ceil((float) (i + 1) / 10.0F) - 1;
      int x = left + i % 10 * 8;
      int y = top - row * rowHeight;

      if (health <= 4) y += this.rand.nextInt(2);
      if (i == this.regen) y -= 2;

      this.blit(matrixStack, x, y, BACKGROUND, TOP, 9, 9);

      if (highlight) {
        if (i * 2 + 1 < healthLast) {
          this.blit(matrixStack, x, y, MARGIN + 54, TOP, 9, 9); //6
        }
        else if (i * 2 + 1 == healthLast) {
          this.blit(matrixStack, x, y, MARGIN + 63, TOP, 9, 9); //7
        }
      }

      if (absorbRemaining > 0.0F) {
        if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
          this.blit(matrixStack, x, y, MARGIN + 153, TOP, 9, 9); //17
          absorbRemaining -= 1.0F;
        }
        else {
          this.blit(matrixStack, x, y, MARGIN + 144, TOP, 9, 9); //16
          absorbRemaining -= 2.0F;
        }
      }
      else {
        if (i * 2 + 1 < health) {
          this.blit(matrixStack, x, y, MARGIN + 36, TOP, 9, 9); //4
        }
        else if (i * 2 + 1 == health) {
          this.blit(matrixStack, x, y, MARGIN + 45, TOP, 9, 9); //5
        }
      }
    }

    this.renderExtraHearts(matrixStack, left, top, player);
    this.renderExtraAbsorption(matrixStack, left, top - rowHeight, player);

    this.mc.getTextureManager().bindTexture(ICON_VANILLA);
    ForgeIngameGui.left_height += 10;
    if (absorb > 0) {
      ForgeIngameGui.left_height += 10;
    }

    event.setCanceled(true);
    RenderSystem.disableBlend();
    this.mc.getProfiler().pop();
    MinecraftForge.EVENT_BUS.post(new RenderGameOverlayEvent.Post(matrixStack, event, HEALTH));
  }

  /**
   * Gets the texture from potion effects
   * @param player  Player instance
   * @return  Texture offset for potion effects
   */
  private int getPotionOffset(PlayerEntity player) {
    int potionOffset = 0;
    StatusEffectInstance potion = player.getStatusEffect(StatusEffects.WITHER);
    if (potion != null) {
      potionOffset = 18;
    }
    potion = player.getStatusEffect(StatusEffects.POISON);
    if (potion != null) {
      potionOffset = 9;
    }
    assert this.mc.world != null;
    if (this.mc.world.getLevelProperties().isHardcore()) {
      potionOffset += 27;
    }
    return potionOffset;
  }

  /**
   * Renders the health above 10 hearts
   * @param matrixStack  Matrix stack instance
   * @param xBasePos     Health bar top corner
   * @param yBasePos     Health bar top corner
   * @param player       Player instance
   */
  private void renderExtraHearts(MatrixStack matrixStack, int xBasePos, int yBasePos, PlayerEntity player) {
    int potionOffset = this.getPotionOffset(player);

    // Extra hearts
    this.mc.getTextureManager().bindTexture(ICON_HEARTS);
    int hp = MathHelper.ceil(player.getHealth());
    this.renderCustomHearts(matrixStack, xBasePos, yBasePos, potionOffset, hp, false);
  }

  /**
   * Renders the absorption health above 10 hearts
   * @param matrixStack  Matrix stack instance
   * @param xBasePos     Health bar top corner
   * @param yBasePos     Health bar top corner
   * @param player       Player instance
   */
  private void renderExtraAbsorption(MatrixStack matrixStack, int xBasePos, int yBasePos, PlayerEntity player) {
    int potionOffset = this.getPotionOffset(player);

    // Extra hearts
    this.mc.getTextureManager().bindTexture(ICON_ABSORB);
    int absorb = MathHelper.ceil(player.getAbsorptionAmount());
    this.renderCustomHearts(matrixStack, xBasePos, yBasePos, potionOffset, absorb, true);
  }

  /**
   * Gets the texture offset from the regen effect
   * @param i       Heart index
   * @param offset  Current offset
   */
  private int getYRegenOffset(int i, int offset) {
    return i + offset == this.regen ? -2 : 0;
  }

  /**
   * Shared logic to render custom hearts
   * @param matrixStack  Matrix stack instance
   * @param xBasePos     Health bar top corner
   * @param yBasePos     Health bar top corner
   * @param potionOffset Offset from the potion effect
   * @param count        Number to render
   * @param absorb       If true, render absorption hearts
   */
  private void renderCustomHearts(MatrixStack matrixStack, int xBasePos, int yBasePos, int potionOffset, int count, boolean absorb) {
    int regenOffset = absorb ? 10 : 0;
    for (int iter = 0; iter < count / 20; iter++) {
      int renderHearts = (count - 20 * (iter + 1)) / 2;
      int heartIndex = iter % 11;
      if (renderHearts > 10) {
        renderHearts = 10;
      }
      for (int i = 0; i < renderHearts; i++) {
        int y = this.getYRegenOffset(i, regenOffset);
        if (absorb) {
          this.blit(matrixStack, xBasePos + 8 * i, yBasePos + y, 0, 54, 9, 9);
        }
        this.blit(matrixStack, xBasePos + 8 * i, yBasePos + y, 18 * heartIndex, potionOffset, 9, 9);
      }
      if (count % 2 == 1 && renderHearts < 10) {
        int y = this.getYRegenOffset(renderHearts, regenOffset);
        if (absorb) {
          this.blit(matrixStack, xBasePos + 8 * renderHearts, yBasePos + y, 0, 54, 9, 9);
        }
        this.blit(matrixStack, xBasePos + 8 * renderHearts, yBasePos + y, 9 + 18 * heartIndex, potionOffset, 9, 9);
      }
    }
  }
}
