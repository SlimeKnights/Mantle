package slimeknights.mantle.client.gui.book.element;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

import org.lwjgl.opengl.GL11;

import java.util.HashMap;
import java.util.List;

import slimeknights.mantle.client.book.data.element.BlockData;
import slimeknights.mantle.client.book.data.element.ItemStackData;

public class ElementStructure extends SizedBookElement {

  private BlockColors blockColors = Minecraft.getMinecraft().getBlockColors();
  private ItemColors itemColors = Minecraft.getMinecraft().getItemColors();
  private BlockAccess world;
  private float scale = 50.0F;
  private float xTranslate = 0F;
  private float yTranslate = 0F;

  private float w = 0F;
  private float h = 0F;

  private float rotX = -11.25F;
  private float rotY = 45F;
  private float rotZ = 0F;

  public ElementStructure(int x, int y, int width, int height, int[] size, BlockData[] structure) {
    super(x, y, width, height);
    world = new BlockAccess(size, structure);

    if(size.length == 3) {
      scale = size[0] > size[1] ? width / size[0] - 10F : height / size[1] - 10F;

      if(scale * size[0] > width) {
        scale = width / size[0] - 10F;
      }

      xTranslate = x + width / 2 - (size[0] * scale) / 2;
      yTranslate = y + height / 2 - (size[1] * scale) / 2;

      w = size[0] * scale;
      h = size[1] * scale;
    }
  }

  @Override
  public void draw(int mouseX, int mouseY, float partialTicks, FontRenderer fontRenderer) {
    GlStateManager.pushMatrix();

    GlStateManager.translate(xTranslate, yTranslate, 500F);

    GlStateManager.cullFace(GlStateManager.CullFace.FRONT);

    // Prepare stencil
    GL11.glEnable(GL11.GL_STENCIL_TEST);
    GL11.glColorMask(false, false, false, false);
    GL11.glDepthMask(false);
    GL11.glStencilFunc(GL11.GL_NEVER, 1, 0xFF);
    GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_KEEP, GL11.GL_KEEP);

    GL11.glStencilMask(0xFF);
    GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex2i(0, 0);
    GL11.glVertex2i(0, height);
    GL11.glVertex2i(width, height);
    GL11.glVertex2i(width, 0);
    GL11.glEnd();

    GL11.glColorMask(true, true, true, true);
    GL11.glDepthMask(true);
    GL11.glStencilMask(0x00);
    GL11.glStencilFunc(GL11.GL_EQUAL, 0, 0xFF);

    GlStateManager.color(0F, 1F, 0F, 1F);
    GL11.glBegin(GL11.GL_QUADS);
    GL11.glVertex2i(0, 0);
    GL11.glVertex2i(0, height);
    GL11.glVertex2i(width, height);
    GL11.glVertex2i(width, 0);
    GL11.glEnd();

    GlStateManager.translate(w / 2, h / 2, 0);
    GlStateManager.rotate(rotX, 1F, 0F, 0F);
    GlStateManager.rotate(rotY, 0F, 1F, 0F);
    GlStateManager.rotate(rotZ, 0F, 0F, 1F);
    GlStateManager.translate(-w / 2, -h / 2, 0);

    for(int x = 0; x < world.getWidth(); x++) {
      for(int y = 0; y < world.getHeight(); y++) {
        for(int z = 0; z < world.getDepth(); z++) {
          BlockPos pos = new BlockPos(x, y, z);
          IBlockState state = world.getBlockState(pos);
          IBakedModel model = mc.getBlockRendererDispatcher().getModelForState(state);

          Block block = state.getBlock();

          if(block == Blocks.AIR) {
            continue;
          }

          GlStateManager.pushMatrix();

          GlStateManager.enableDepth();
          mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

          if(block == null) {
            return;
          }

          int color = blockColors.colorMultiplier(state, world, pos, 0);
          float red = (color >> 16 & 255) / 255.0F;
          float green = (color >> 8 & 255) / 255.0F;
          float blue = (color & 255) / 255.0F;
          GlStateManager.color(red, green, blue);

          GlStateManager.scale(scale, scale, scale);

          GlStateManager.translate(x, world.getHeight() - y, z);
          BlockModelRenderer render = mc.getBlockRendererDispatcher().getBlockModelRenderer();

          // 1.9 remove this?
          //block.setBlockBoundsBasedOnState(world, pos);
          int i = blockColors.colorMultiplier(state, world, pos, 0);
          GlStateManager.rotate(90F, 0, 1, 0);

          if(EntityRenderer.anaglyphEnable) {
            i = TextureUtil.anaglyphColor(i);
          }

          float r = (float) (i >> 16 & 255) / 255.0F;
          float g = (float) (i >> 8 & 255) / 255.0F;
          float b = (float) (i & 255) / 255.0F;

          render.renderModelBrightnessColor(model, blockColors.colorMultiplier(state, world, pos, 0), r, g, b);

          GlStateManager.color(1F, 1F, 1F);
          GlStateManager.popMatrix();
          renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
        }
      }
    }

    GlStateManager.disableLighting();
    GlStateManager.cullFace(GlStateManager.CullFace.BACK);
    GL11.glDisable(GL11.GL_STENCIL_TEST);
    GlStateManager.popMatrix();
  }

  private void setupGuiTransform(int xPosition, int yPosition) {
    GlStateManager.translate((float) xPosition, (float) yPosition, 100.0F + this.zLevel);
    GlStateManager.translate(8.0F, 8.0F, 0.0F);
    GlStateManager.scale(1.0F, 1.0F, -1.0F);
    GlStateManager.scale(0.5F, 0.5F, 0.5F);

    GlStateManager.scale(40.0F, 40.0F, 40.0F);
    GlStateManager.rotate(210.0F, 1.0F, 0.0F, 0.0F);
    GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
    GlStateManager.enableLighting();
  }

  private void renderQuads(VertexBuffer renderer, List<BakedQuad> quads, int color, BlockPos pos) {
    boolean flag = color == -1;
    int i = 0;

    for(int j = quads.size(); i < j; ++i) {
      BakedQuad bakedquad = quads.get(i);
      int k = color;

      if(flag && bakedquad.hasTintIndex()) {
        Block block = world.getBlockState(pos).getBlock();
        k = blockColors.colorMultiplier(world.getBlockState(pos), world, pos, 0);

        if(EntityRenderer.anaglyphEnable) {
          k = TextureUtil.anaglyphColor(k);
        }

        k = k | -16777216;
      }

      net.minecraftforge.client.model.pipeline.LightUtil.renderQuadColor(renderer, bakedquad, k);
    }
  }

  private class BlockAccess implements IBlockAccess {

    private final int[][][] blocks;
    private final byte[][][] meta;
    private final int[] size;
    private HashMap<Integer[], TileEntity> tileEntityMap = new HashMap<>();

    public BlockAccess(int[] size, BlockData[] structure) {
      this.size = size;

      blocks = new int[size[0]][size[1]][size[2]];
      meta = new byte[size[0]][size[1]][size[2]];

      for(BlockData block : structure) {
        Block tile = Block.getBlockFromName(block.block);
        if(tile == null) {
          continue;
        }
        byte metadata = block.meta >= 0 && block.meta < 16 ? block.meta : 0;

        if(block.pos != null && block.pos.length == 3 && block.endPos != null && block.endPos.length == 3) {
          for(int x = block.pos[0]; x <= block.endPos[0]; x++) {
            for(int y = block.pos[1]; y <= block.endPos[1]; y++) {
              for(int z = block.pos[2]; z <= block.endPos[2]; z++) {
                if(x >= size[0] || y >= size[1] || z >= size[2]) {
                  continue;
                }

                blocks[x][y][z] = Block.getIdFromBlock(tile);
                meta[x][y][z] = metadata;

                TileEntity te = tile.hasTileEntity(tile.getStateFromMeta(metadata)) ? tile
                    .createTileEntity(Minecraft.getMinecraft().theWorld, tile.getStateFromMeta(metadata)) : null;
                if(te != null) {
                  te.setPos(new BlockPos(x, y, z));
                  tileEntityMap.put(new Integer[]{x, y, z}, te);

                  if(block.nbt != null) {
                    try {
                      te.readFromNBT(JsonToNBT.getTagFromJson(ItemStackData.filterJsonQuotes(block.nbt.toString())));
                    } catch(NBTException e) {
                      e.printStackTrace();
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
      if(!isValid(pos)) {
        return null;
      }
      return tileEntityMap.get(new Integer[]{pos.getX(), pos.getY(), pos.getZ()});
    }

    @Override
    public int getCombinedLight(BlockPos pos, int lightValue) {
      return 15;
    }

    @Override
    public IBlockState getBlockState(BlockPos pos) {
      if(!isValid(pos)) {
        return Blocks.AIR.getDefaultState();
      }
      Block block = Block.getBlockById(blocks[pos.getX()][pos.getY()][pos.getZ()]);
      if(block == null) {
        return Blocks.AIR.getDefaultState();
      }

      return block.getActualState(block.getStateFromMeta(meta[pos.getX()][pos.getY()][pos.getZ()]), this, pos);
    }

    @Override
    public boolean isAirBlock(BlockPos pos) {
      return !isValid(pos) || Block.getBlockById(blocks[pos.getX()][pos.getY()][pos.getZ()]) == Blocks.AIR;
    }

    @Override
    public Biome getBiome(BlockPos blockPos) {
      return Biomes.JUNGLE;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
      return false;
    }

    @Override
    public int getStrongPower(BlockPos pos, EnumFacing direction) {
      if(!isValid(pos)) {
        return 0;
      }

      IBlockState iblockstate = this.getBlockState(pos);
      return iblockstate.getBlock().getStrongPower(iblockstate, this, pos, direction);
    }

    @Override
    public WorldType getWorldType() {
      return WorldType.DEFAULT;
    }

    @Override
    public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
      if(!this.isValid(pos)) {
        return _default;
      }

      return getBlockState(pos).getBlock().isSideSolid(this.getBlockState(pos), this, pos, side);
    }

    public boolean isValid(BlockPos pos) {
      return pos != null && pos.getX() < size[0] && pos.getY() < size[1] && pos.getZ() < size[2] && pos.getX() >= 0 && pos.getY() >= 0 && pos.getZ() >= 0;
    }

    public int getWidth() {
      return size[0];
    }

    public int getHeight() {
      return size[1];
    }

    public int getDepth() {
      return size[2];
    }
  }
}
