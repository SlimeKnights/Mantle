package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import slimeknights.mantle.client.book.StructureBlockAccess;
import slimeknights.mantle.client.book.StructureInfo;
import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.Random;
import java.util.stream.IntStream;

public class ElementStructure extends SizedBookElement {

  private float scale = 50.0F;
  private float xTranslate = 0F;
  private float yTranslate = 0F;

  private float w = 0F;
  private float h = 0F;

  public ElementStructure(int x, int y, int width, int height, int[] size, BlockData[] structure) {
    super(x, y, width, height);

    if (size.length == 3) {
      this.scale = 100f / (float) IntStream.of(size).max().getAsInt();

      float sx = (float) width / (float) BookScreen.PAGE_WIDTH;
      float sy = (float) height / (float) BookScreen.PAGE_HEIGHT;

      this.scale *= Math.min(sx, sy);

      this.xTranslate = x + width / 2;// - (size[0] * scale) / 2;
      this.yTranslate = y + height / 2;// - (size[1] * scale) / 2;

      this.w = size[0] * this.scale;
      this.h = size[1] * this.scale;
    }

    this.init(size, structure);
  }

  boolean canTick = false;
  int tick = 0;

  float rotX = 0;
  float rotY = 0;
  float rotZ = 0;

  public StructureInfo structureData;
  StructureBlockAccess blockAccess;

  public void init(int[] size, BlockData[] data) {
    int yOff = 0;

    this.structureData = new StructureInfo(size[0], size[1], size[2], data);
    this.blockAccess = new StructureBlockAccess(this.structureData);

    this.rotX = 25;
    this.rotY = -45;
/*
    boolean canRenderFormed = multiblock.canRenderFormedStructure();
    //			yOff = (structureHeight-1)*12+structureWidth*5+structureLength*5+16;
    //			yOff = Math.max(48, yOff);
    float f = (float)Math.sqrt(structureHeight*structureHeight + structureWidth*structureWidth + structureLength*structureLength);
    float scale = multiblock.getManualScale();
    yOff = (int)(multiblock.getManualScale()*Math.sqrt(structureHeight*structureHeight + structureWidth*structureWidth + structureLength*structureLength));
    yOff = Math.max(10+(canRenderFormed?12:0)+(structureHeight>1?36:0), yOff);
    yOff = 10+Math.max(10+(multiblock.canRenderFormedStructure()?12:0)+(structureHeight>1?36:0), (int) (f*scale));
    pageButtons.add(new GuiButtonManualNavigation(gui, 100, x+4,y+yOff/2-(canRenderFormed?11:5), 10,10, 4));
    if(canRenderFormed)
      pageButtons.add(new GuiButtonManualNavigation(gui, 103, x+4,y+yOff/2+1, 10,10, 6));
    if(structureHeight>1)
    {
      pageButtons.add(new GuiButtonManualNavigation(gui, 101, x+4,y+yOff/2-(canRenderFormed?14:8)-16, 10,16, 3));
      pageButtons.add(new GuiButtonManualNavigation(gui, 102, x+4,y+yOff/2+(canRenderFormed?14:8), 10,16, 2));
    }
/*
    IngredientStack[] totalMaterials = this.multiblock.getTotalMaterials();
    if(false && false)
    {
      componentTooltip = new ArrayList();
      componentTooltip.add(I18n.format("desc.immersiveengineering.info.reqMaterial"));
      int maxOff = 1;
      boolean hasAnyItems = false;
      boolean[] hasItems = new boolean[totalMaterials.length];
      for(int ss = 0; ss < totalMaterials.length; ss++)
        if(totalMaterials[ss] != null)
        {
          IngredientStack req = totalMaterials[ss];
          int reqSize = req.inputSize;
          for(int slot = 0; slot < ManualUtils.mc().thePlayer.inventory.getSizeInventory(); slot++)
          {
            ItemStack inSlot = ManualUtils.mc().thePlayer.inventory.getStackInSlot(slot);
            if(inSlot != null && req.matchesItemStackIgnoringSize(inSlot))
              if((reqSize -= inSlot.stackSize) <= 0)
                break;
          }
          if(reqSize <= 0)
          {
            hasItems[ss] = true;
            if(!hasAnyItems)
              hasAnyItems = true;
          }
          maxOff = Math.max(maxOff, ("" + req.inputSize).length());
        }
      for(int ss = 0; ss < totalMaterials.length; ss++)
        if(totalMaterials[ss] != null)
        {
          IngredientStack req = totalMaterials[ss];
          int indent = maxOff - ("" + req.inputSize).length();
          String sIndent = "";
          if(indent > 0)
            for(int ii = 0; ii < indent; ii++)
              sIndent += "0";
          String s = hasItems[ss] ? (TextFormatting.GREEN + TextFormatting.BOLD.toString() + "\u2713" + TextFormatting.RESET + " ") : hasAnyItems ? ("   ") : "";
          s += TextFormatting.GRAY + sIndent + req.inputSize + "x " + TextFormatting.RESET;
          ItemStack example = req.getExampleStack();
          if(example != null)
            s += example.getRarity().rarityColor + example.getDisplayName();
          else
            s += "???";
          componentTooltip.add(s);
        }
    }*/
    //    super.initPage(gui, x, y+yOff, pageButtons);
  }

  int[] lastClick = null;
  private int fullStructureSteps = 5;

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    if (this.lastClick != null) {
      if (Minecraft.getInstance().mouseHelper.isLeftDown() || Minecraft.getInstance().mouseHelper.isRightDown()) {
        int dx = mouseX - this.lastClick[0];
        int dy = mouseY - this.lastClick[1];
        float maxSpeed = 10f;
        float changeX = Math.min(maxSpeed, dx / 10f);
        float changeY = Math.min(maxSpeed, dy / 10f);

        this.rotY += changeX;
        this.rotX += changeY;
      }
      else {
        this.lastClick = null;
      }
    }

    if (this.canTick) {
      if (++this.tick % 20 == 0) {
        if (this.structureData.canStep() || ++this.fullStructureSteps >= 5) {
          this.structureData.step();
          this.fullStructureSteps = 0;
        }
      }
    }
    else {
      this.structureData.reset();
      this.structureData.setShowLayer(9);
    }

    int structureLength = this.structureData.structureLength;
    int structureWidth = this.structureData.structureWidth;
    int structureHeight = this.structureData.structureHeight;

    int xHalf = (structureWidth * 5 - structureLength * 5);
    int yOffPartial = (structureHeight - 1) * 16 + structureWidth * 8 + structureLength * 8;
    int yOffTotal = Math.max(52, yOffPartial + 16);

    GlStateManager.enableRescaleNormal();
    GlStateManager.pushMatrix();
    RenderHelper.disableStandardItemLighting();
    //			GL11.glEnable(GL11.GL_DEPTH_TEST);
    //			GL11.glDepthFunc(GL11.GL_ALWAYS);
    //			GL11.glDisable(GL11.GL_CULL_FACE);
    int i = 0;
    ItemStack highlighted = null;

    final BlockRendererDispatcher blockRender = Minecraft.getInstance().getBlockRendererDispatcher();

    float f = (float) Math.sqrt(structureHeight * structureHeight + structureWidth * structureWidth + structureLength * structureLength);
    yOffTotal = 10 + Math.max(10 + (structureHeight > 1 ? 36 : 0), (int) (f * this.scale));
    //GlStateManager.translate(x + 60, y + 10 + f / 2 * scale, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
    GlStateManager.translatef(this.xTranslate, this.yTranslate, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
    // todo: translate where it actually needs to be and to counter z-layer of the book
    GlStateManager.scalef(this.scale, -this.scale, 1);
    GlStateManager.rotatef(this.rotX, 1, 0, 0);
    GlStateManager.rotatef(this.rotY, 0, 1, 0);

    GlStateManager.translatef((float) structureLength / -2f, (float) structureHeight / -2f, (float) structureWidth / -2f);

    GlStateManager.disableLighting();

    if (Minecraft.isAmbientOcclusionEnabled()) {
      GlStateManager.shadeModel(GL11.GL_SMOOTH);
    }
    else {
      GlStateManager.shadeModel(GL11.GL_FLAT);
    }

    if (structureWidth % 2 == 1) {
      //GlStateManager.translate(-.5f, 0, 0);
    }
    int iterator = 0;

    this.mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
    for (int h = 0; h < this.structureData.structureHeight; h++) {
      for (int l = 0; l < this.structureData.structureLength; l++) {
        for (int w = 0; w < this.structureData.structureWidth; w++) {
          BlockPos pos = new BlockPos(l, h, w);
          if (!this.blockAccess.isAirBlock(pos)) {
            BlockState state = this.blockAccess.getBlockState(pos);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
            Random random = new Random();
            blockRender.renderBlock(state, pos, this.blockAccess, buffer, random, net.minecraftforge.client.model.data.EmptyModelData.INSTANCE);
            tessellator.draw();
          }
        }
      }
    }
    //			GL11.glTranslated(0, 0, -i);
    GlStateManager.popMatrix();

    RenderHelper.disableStandardItemLighting();
    GlStateManager.disableRescaleNormal();
    GlStateManager.shadeModel(GL11.GL_FLAT);
    GlStateManager.enableBlend();
    RenderHelper.disableStandardItemLighting();
/*
    fontRenderer.setUnicodeFlag(true);
    //if(localizedText!=null&&!localizedText.isEmpty())
    //fontRenderer.drawSplitString(localizedText, x,y+yOffTotal, 120, manual.getTextColour());

    fontRenderer.setUnicodeFlag(false);
    if(componentTooltip != null) {
      //fontRenderer.drawString("?", x + 116, y + yOffTotal / 2 - 4, manual.getTextColour(), false);
      fontRenderer.drawString("?", x + 116, y + yOffTotal / 2 - 4, 0x000000, false);
      if(mouseX >= x + 116 && mouseX < x + 122 && mouseY >= y + yOffTotal / 2 - 4 && mouseY < y + yOffTotal / 2 + 4) {
        this.drawHoveringText(componentTooltip, mouseX, mouseY, fontRenderer);
      }
    }*/
  }

  private int lastX;
  private int lastY;

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);

    //lastX = mouseX;
    //lastY = mouseY;
    this.lastClick = new int[] { (int) mouseX, (int) mouseY };
  }

  @Override
  public void mouseClickMove(double mouseX, double mouseY, int clickedMouseButton) {
    int dx = (int) mouseX - this.lastX;
    int dy = (int) mouseX - this.lastY;

    float maxSpeed = 1f;
    float changeX = Math.min(maxSpeed, dx / 100f);
    float changeY = Math.min(maxSpeed, dy / 100f);

    //rotX += changeX;
    //rotY += changeX;

    //rotY = rotY + (dx / 104f) * 10;
    //rotX = rotX + (dy / 100f) * 10;

    //lastX = mouseX;
    //lastY = mouseY;
  }

  @Override
  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {
    super.mouseReleased(mouseX, mouseY, clickedMouseButton);
    this.lastClick = null;
  }

  @Override
  public void mouseDragged(int clickX, int clickY, int mx, int my, int lastX, int lastY, int button) {
    //if((clickX >= 40 && clickX < 144 && mx >= 20 && mx < 164) && (clickY >= 30 && clickY < 130 && my >= 30 && my < 180)) {
    int dx = mx - lastX;
    int dy = my - lastY;
    this.rotY = this.rotY + (dx / 104f);// * 80;
    this.rotX = this.rotX + (dy / 100f);// * 80;
    //}
  }
/*
  @Override
  public void buttonPressed(GuiManual gui, GuiButton button)
  {
    if(button.id==100)
    {
      canTick = !canTick;
      ((GuiButtonManualNavigation)button).type = ((GuiButtonManualNavigation)button).type == 4 ? 5 : 4;
    }
    else if(button.id==101)
    {
      showLayer = Math.min(showLayer+1, structureHeight-1);
      tick= (countPerLevel[showLayer])*40;
    }
    else if(button.id==102)
    {
      showLayer = Math.max(showLayer-1, -1);
      tick= (showLayer==-1?blockCount:countPerLevel[showLayer])*40;
    }
    else if(button.id==103)
      showCompleted = !showCompleted;
    super.buttonPressed(gui, button);
  }
*/

  public void changeActiveLayer(int direction) {

  }
}
