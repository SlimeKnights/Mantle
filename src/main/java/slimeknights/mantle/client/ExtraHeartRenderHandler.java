package slimeknights.mantle.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

import slimeknights.mantle.Mantle;

@SideOnly(Side.CLIENT)
public class ExtraHeartRenderHandler {
  private static final ResourceLocation ICON_HEARTS = new ResourceLocation(Mantle.modId, "textures/gui/hearts.png");
  private static final ResourceLocation ICON_ABSORB = new ResourceLocation(Mantle.modId, "textures/gui/absorb.png");
  private static final ResourceLocation ICON_VANILLA = Gui.ICONS;

  private final Minecraft mc = Minecraft.getMinecraft();

  private int updateCounter = 0;
  private int playerHealth = 0;
  private int lastPlayerHealth = 0;
  private long healthUpdateCounter = 0;
  private long lastSystemTime = 0;
  private Random rand = new Random();

  private int height;
  private int width;
  private int regen;

  private static int left_height = 39;

  public void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
    Minecraft.getMinecraft().ingameGUI.drawTexturedModalRect(x, y, textureX, textureY, width, height);
  }

  /* HUD */
  @SubscribeEvent(priority = EventPriority.LOW)
  public void renderHealthbar(RenderGameOverlayEvent.Pre event) {
    if(event.getType() != RenderGameOverlayEvent.ElementType.HEALTH || event.isCanceled()) {
      return;
    }
    // extra setup stuff from us
    left_height = 39;
    ScaledResolution resolution = event.getResolution();
    width = resolution.getScaledWidth();
    height = resolution.getScaledHeight();
    event.setCanceled(true);
    updateCounter = mc.ingameGUI.getUpdateCounter();

    // start default forge/mc rendering
    // changes are indicated by comment
    mc.mcProfiler.startSection("health");
    GlStateManager.enableBlend();

    EntityPlayer player = (EntityPlayer)this.mc.getRenderViewEntity();
    int health = MathHelper.ceiling_float_int(player.getHealth());
    boolean highlight = healthUpdateCounter > (long)updateCounter && (healthUpdateCounter - (long)updateCounter) / 3L %2L == 1L;

    if (health < this.playerHealth && player.hurtResistantTime > 0)
    {
      this.lastSystemTime = Minecraft.getSystemTime();
      this.healthUpdateCounter = (long)(this.updateCounter + 20);
    }
    else if (health > this.playerHealth && player.hurtResistantTime > 0)
    {
      this.lastSystemTime = Minecraft.getSystemTime();
      this.healthUpdateCounter = (long)(this.updateCounter + 10);
    }

    if (Minecraft.getSystemTime() - this.lastSystemTime > 1000L)
    {
      this.playerHealth = health;
      this.lastPlayerHealth = health;
      this.lastSystemTime = Minecraft.getSystemTime();
    }

    this.playerHealth = health;
    int healthLast = this.lastPlayerHealth;

    IAttributeInstance attrMaxHealth = player.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
    float healthMax = (float)attrMaxHealth.getAttributeValue();
    float absorb = MathHelper.ceiling_float_int(player.getAbsorptionAmount());

    // CHANGE: simulate 10 hearts max if there's more, so vanilla only renders one row max
    healthMax = Math.min(healthMax, 20f);
    health = Math.min(health, 20);
    absorb = Math.min(absorb, 20);

    int healthRows = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F / 10.0F);
    int rowHeight = Math.max(10 - (healthRows - 2), 3);

    this.rand.setSeed((long)(updateCounter * 312871));

    int left = width / 2 - 91;
    int top = height - left_height;
    left_height += (healthRows * rowHeight);
    if (rowHeight != 10) left_height += 10 - rowHeight;

    regen = -1;
    if (player.isPotionActive(MobEffects.REGENERATION))
    {
      regen = updateCounter % 25;
    }

    final int TOP =  9 * (mc.theWorld.getWorldInfo().isHardcoreModeEnabled() ? 5 : 0);
    final int BACKGROUND = (highlight ? 25 : 16);
    int MARGIN = 16;
    if (player.isPotionActive(MobEffects.POISON))      MARGIN += 36;
    else if (player.isPotionActive(MobEffects.WITHER)) MARGIN += 72;
    float absorbRemaining = absorb;

    for (int i = MathHelper.ceiling_float_int((healthMax + absorb) / 2.0F) - 1; i >= 0; --i)
    {
      //int b0 = (highlight ? 1 : 0);
      int row = MathHelper.ceiling_float_int((float)(i + 1) / 10.0F) - 1;
      int x = left + i % 10 * 8;
      int y = top - row * rowHeight;

      if (health <= 4) y += rand.nextInt(2);
      if (i == regen) y -= 2;

      drawTexturedModalRect(x, y, BACKGROUND, TOP, 9, 9);

      if (highlight)
      {
        if (i * 2 + 1 < healthLast)
          drawTexturedModalRect(x, y, MARGIN + 54, TOP, 9, 9); //6
        else if (i * 2 + 1 == healthLast)
          drawTexturedModalRect(x, y, MARGIN + 63, TOP, 9, 9); //7
      }

      if (absorbRemaining > 0.0F)
      {
        if (absorbRemaining == absorb && absorb % 2.0F == 1.0F)
        {
          drawTexturedModalRect(x, y, MARGIN + 153, TOP, 9, 9); //17
          absorbRemaining -= 1.0F;
        }
        else
        {
          drawTexturedModalRect(x, y, MARGIN + 144, TOP, 9, 9); //16
          absorbRemaining -= 2.0F;
        }
      }
      else
      {
        if (i * 2 + 1 < health)
          drawTexturedModalRect(x, y, MARGIN + 36, TOP, 9, 9); //4
        else if (i * 2 + 1 == health)
          drawTexturedModalRect(x, y, MARGIN + 45, TOP, 9, 9); //5
      }
    }


    renderExtraHearts(left, top, player);
    renderExtraAbsorption(left, top - rowHeight, player);


    this.mc.getTextureManager().bindTexture(ICON_VANILLA);
    GuiIngameForge.left_height += 10;
    if(absorb > 0) {
      GuiIngameForge.left_height += 10;
    }

    event.setCanceled(true);

    GlStateManager.disableBlend();
    mc.mcProfiler.endSection();
  }

  private void renderExtraHearts(int xBasePos, int yBasePos, EntityPlayer player) {
    int potionOffset = getPotionOffset(player);

    // Extra hearts
    this.mc.getTextureManager().bindTexture(ICON_HEARTS);

    int hp = MathHelper.ceiling_float_int(player.getHealth());
    renderCustomHearts(xBasePos, yBasePos, potionOffset, hp, false);
  }

  private void renderCustomHearts(int xBasePos, int yBasePos, int potionOffset, int count, boolean absorb) {
    int regenOffset = absorb ? 10 : 0;
    for(int iter = 0; iter < count / 20; iter++) {
      int renderHearts = (count - 20 * (iter + 1)) / 2;
      if(renderHearts > 10) {
        renderHearts = 10;
      }
      for(int i = 0; i < renderHearts; i++) {
        int y = 0;
        if(i + regenOffset == regen) {
          y -= 2;
        }
        if(absorb) {
          this.drawTexturedModalRect(xBasePos + 8 * i, yBasePos + y, 0, 54, 9, 9);
        }
        this.drawTexturedModalRect(xBasePos + 8 * i, yBasePos + y, 0 + 18 * iter, potionOffset, 9, 9);
      }
      if(count % 2 == 1 && renderHearts < 10) {
        if(absorb) {
          this.drawTexturedModalRect(xBasePos + 8 * renderHearts, yBasePos, 0, 54, 9, 9);
        }
        this.drawTexturedModalRect(xBasePos + 8 * renderHearts, yBasePos, 9 + 18 * iter, potionOffset, 9, 9);
      }
    }
  }

  private int getPotionOffset(EntityPlayer player) {
    int potionOffset = 0;
    PotionEffect potion = player.getActivePotionEffect(MobEffects.WITHER);
    if(potion != null) {
      potionOffset = 18;
    }
    potion = player.getActivePotionEffect(MobEffects.POISON);
    if(potion != null) {
      potionOffset = 9;
    }
    if(mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
      potionOffset += 27;
    }
    return potionOffset;
  }

  private void renderExtraAbsorption(int xBasePos, int yBasePos, EntityPlayer player) {
    int potionOffset = getPotionOffset(player);

    // Extra hearts
    this.mc.getTextureManager().bindTexture(ICON_ABSORB);

    int absorb = MathHelper.ceiling_float_int(player.getAbsorptionAmount());
    renderCustomHearts(xBasePos, yBasePos, potionOffset, absorb, true);
  }
}
