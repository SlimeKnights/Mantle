package slimeknights.mantle.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.fluid.FluidCuboid;
import slimeknights.mantle.client.model.fluid.FluidCuboid.FluidFace;

import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FluidRenderer {
  /** Render type used for rendering fluids */
  public static final RenderType RENDER_TYPE = RenderType.makeType(
      Mantle.modId + ":block_render_type",
      DefaultVertexFormats.POSITION_COLOR_TEX_LIGHTMAP, 7, 256, true, false,
      RenderType.State.getBuilder().texture(new RenderState.TextureState(PlayerContainer.LOCATION_BLOCKS_TEXTURE, false, false))
                      .shadeModel(RenderType.SHADE_ENABLED)
                      .lightmap(RenderType.LIGHTMAP_ENABLED)
                      .texture(RenderType.BLOCK_SHEET_MIPPED)
                      .transparency(RenderType.TRANSLUCENT_TRANSPARENCY)
                      .build(false));

  /**
   * Gets a block sprite from the given location
   * @param sprite  Sprite name
   * @return  Sprite location
   */
  public static TextureAtlasSprite getBlockSprite(ResourceLocation sprite) {
    return Minecraft.getInstance().getModelManager().getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE).getSprite(sprite);
  }

  /**
   * Takes the larger light value between combinedLight and the passed block light
   * @param combinedLight  Sky light/block light lightmap value
   * @param blockLight     New 0-15 block light value
   * @return  Updated packed light including the new light value
   */
  public static int withBlockLight(int combinedLight, int blockLight) {
    // skylight from the combined plus larger block light between combined and parameter
    // not using methods from LightTexture to reduce number of operations
    return (combinedLight & 0xFFFF0000) | Math.max(blockLight << 4, combinedLight & 0xFFFF);
  }

  /* Fluid cuboids */

  /**
   * Forces the UV to be between 0 and 1
   * @param value  Original value
   * @param upper  If true, this is the larger UV. Needed to enforce integer values end up at 1
   * @return  UV mapped between 0 and 1
   */
  private static float boundUV(float value, boolean upper) {
    value = value % 1;
    if (value == 0) {
      // if it lands exactly on the 0 bound, map that to 1 instead for the larger UV
      return upper ? 1 : 0;
    }
    // modulo returns a negative result if the input is negative, so add 1 to account for that
    return value < 0 ? (value + 1) : value;
  }

  /**
   * Adds a quad to the renderer
   * @param renderer    Renderer instnace
   * @param matrix      Render matrix
   * @param sprite      Sprite to render
   * @param from        Quad start
   * @param to          Quad end
   * @param face        Face to render
   * @param color       Color to use in rendering
   * @param brightness  Face brightness
   * @param flowing     If true, half texture coordinates
   */
  public static void putTexturedQuad(IVertexBuilder renderer, Matrix4f matrix, TextureAtlasSprite sprite, Vector3f from, Vector3f to, Direction face, int color, int brightness, int rotation, boolean flowing) {
    // start with texture coordinates
    float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();
    float x2 = to.getX(), y2 = to.getY(), z2 = to.getZ();
    // choose UV based on opposite two axis
    float u1, u2, v1, v2;
    switch (face) {
      case DOWN:
      default:
        u1 = x1; u2 = x2;
        v1 = z2; v2 = z1;
        break;
      case UP:
        u1 = x1; u2 = x2;
        v1 = -z1; v2 = -z2;
        break;
      case NORTH:
        u1 = -x1; u2 = -x2;
        v1 = y1; v2 = y2;
        break;
      case SOUTH:
        u1 = x2; u2 = x1;
        v1 = y1; v2 = y2;
        break;
      case WEST:
        u1 = z2; u2 = z1;
        v1 = y1; v2 = y2;
        break;
      case EAST:
        u1 = -z1; u2 = -z2;
        v1 = y1; v2 = y2;
        break;
    }

    // flip V when relevant
    if (rotation == 0 || rotation == 270) {
      float temp = v1;
      v1 = -v2;
      v2 = -temp;
    }
    // flip U when relevant
    if (rotation >= 180) {
      float temp = u1;
      u1 = -u2;
      u2 = -temp;
    }
    
    // bound UV to be between 0 and 1
    boolean reverse = u1 > u2;
    u1 = boundUV(u1, reverse);
    u2 = boundUV(u2, !reverse);
    reverse = v1 > v2;
    v1 = boundUV(v1, reverse);
    v2 = boundUV(v2, !reverse);

    // if rotating by 90 or 270, swap U and V
    float minU, maxU, minV, maxV;
    double size = flowing ? 8 : 16;
    if ((rotation % 180) == 90) {
      minU = sprite.getInterpolatedU(v1 * size);
      maxU = sprite.getInterpolatedU(v2 * size);
      minV = sprite.getInterpolatedV(u1 * size);
      maxV = sprite.getInterpolatedV(u2 * size);
    } else {
      minU = sprite.getInterpolatedU(u1 * size);
      maxU = sprite.getInterpolatedU(u2 * size);
      minV = sprite.getInterpolatedV(v1 * size);
      maxV = sprite.getInterpolatedV(v2 * size);
    }
    // based on rotation, put coords into place
    float u3, u4, v3, v4;
    switch(rotation) {
      case 0:
      default:
        u1 = minU; v1 = maxV;
        u2 = minU; v2 = minV;
        u3 = maxU; v3 = minV;
        u4 = maxU; v4 = maxV;
        break;
      case 90:
        u1 = minU; v1 = minV;
        u2 = maxU; v2 = minV;
        u3 = maxU; v3 = maxV;
        u4 = minU; v4 = maxV;
        break;
      case 180:
        u1 = maxU; v1 = minV;
        u2 = maxU; v2 = maxV;
        u3 = minU; v3 = maxV;
        u4 = minU; v4 = minV;
        break;
      case 270:
        u1 = maxU; v1 = maxV;
        u2 = minU; v2 = maxV;
        u3 = minU; v3 = minV;
        u4 = maxU; v4 = minV;
        break;
    }
    // add quads
    int light1 = brightness & 0xFFFF;
    int light2 = brightness >> 0x10 & 0xFFFF;
    int a = color >> 24 & 0xFF;
    int r = color >> 16 & 0xFF;
    int g = color >> 8 & 0xFF;
    int b = color & 0xFF;
    switch (face) {
      case DOWN:
        renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
      case UP:
        renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
      case NORTH:
        renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
      case SOUTH:
        renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
      case WEST:
        renderer.pos(matrix, x1, y1, z2).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y2, z2).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y2, z1).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x1, y1, z1).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
      case EAST:
        renderer.pos(matrix, x2, y1, z1).color(r, g, b, a).tex(u1, v1).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z1).color(r, g, b, a).tex(u2, v2).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y2, z2).color(r, g, b, a).tex(u3, v3).lightmap(light1, light2).endVertex();
        renderer.pos(matrix, x2, y1, z2).color(r, g, b, a).tex(u4, v4).lightmap(light1, light2).endVertex();
        break;
    }
  }

  /**
   * Renders a full fluid cuboid for the given data
   * @param matrices  Matrix stack instance
   * @param buffer    Buffer type
   * @param still     Still sprite
   * @param flowing   Flowing sprite
   * @param cube      Fluid cuboid
   * @param from      Fluid start
   * @param to        Fluid end
   * @param color     Fluid color
   * @param light     Quad lighting
   * @param isGas     If true, fluid is a gas
   */
  public static void renderCuboid(MatrixStack matrices, IVertexBuilder buffer, FluidCuboid cube, TextureAtlasSprite still, TextureAtlasSprite flowing, Vector3f from, Vector3f to, int color, int light, boolean isGas) {
    Matrix4f matrix = matrices.getLast().getMatrix();
    int rotation = isGas ? 180 : 0;
    for (Direction dir : Direction.values()) {
      FluidFace face = cube.getFace(dir);
      if (face != null) {
        boolean isFlowing = face.isFlowing();
        int faceRot = (rotation + face.getRotation()) % 360;
        putTexturedQuad(buffer, matrix, isFlowing ? flowing : still, from, to, dir, color, light, faceRot, isFlowing);
      }
    }
  }

  /**
   * Renders a list of fluid cuboids
   * @param matrices  Matrix stack instance
   * @param buffer    Buffer instance
   * @param cubes     List of cubes to render
   * @param fluid     Fluid to use in rendering
   * @param light     Light level from TER
   */
  public static void renderCuboids(MatrixStack matrices, IVertexBuilder buffer, List<FluidCuboid> cubes, FluidStack fluid, int light) {
    if (fluid.isEmpty()) {
      return;
    }

    // fluid attributes, fetch once for all fluids to save effort
    FluidAttributes attributes = fluid.getFluid().getAttributes();
    TextureAtlasSprite still = getBlockSprite(attributes.getStillTexture(fluid));
    TextureAtlasSprite flowing = getBlockSprite(attributes.getFlowingTexture(fluid));
    int color = attributes.getColor(fluid);
    light = withBlockLight(light,attributes.getLuminosity(fluid));
    boolean isGas = attributes.isGaseous(fluid);

    // render all given cuboids
    for (FluidCuboid cube : cubes) {
      renderCuboid(matrices, buffer, cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, isGas);
    }
  }

  /**
   * Renders a fluid cuboid with the given offset, used to manually place cuboids from a list for rendering {@link #renderCuboids(MatrixStack, IVertexBuilder, List, FluidStack, int)}
   * @param matrices  Matrix stack instance
   * @param buffer    Buffer type
   * @param cube      Fluid cuboid
   * @param yOffset   Amount to offset the cube in the Y direction, used in faucets for rendering fluid in lower block
   * @param still     Still sprite
   * @param flowing   Flowing sprite
   * @param color     Fluid color
   * @param light     Quad lighting from TER
   * @param isGas     If true, fluid is a gas
   */
  public static void renderCuboid(MatrixStack matrices, IVertexBuilder buffer, FluidCuboid cube, float yOffset, TextureAtlasSprite still, TextureAtlasSprite flowing, int color, int light, boolean isGas) {
    if (yOffset != 0) {
      matrices.push();
      matrices.translate(0, yOffset, 0);
    }
    renderCuboid(matrices, buffer, cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, isGas);
    if (yOffset != 0) {
      matrices.pop();
    }
  }

  /**
   * Renders a fluid cuboid with partial height based on the capacity
   * @param matrices  Matrix stack instance
   * @param buffer    Render type buffer instance
   * @param fluid     Fluid to render
   * @param offset    Fluid amount offset, used to animate transitions
   * @param capacity  Fluid tank capacity, must be above 0
   * @param light     Quad lighting from TER
   * @param cube      Fluid cuboid instance
   * @param flipGas   If true, flips gas cubes
   */
  public static void renderScaledCuboid(MatrixStack matrices, IRenderTypeBuffer buffer, FluidCuboid cube, FluidStack fluid, float offset, int capacity, int light, boolean flipGas) {
    // nothing to render
    if (fluid.isEmpty() || capacity <= 0) {
      return;
    }

    // fluid attributes
    FluidAttributes attributes = fluid.getFluid().getAttributes();
    TextureAtlasSprite still = getBlockSprite(attributes.getStillTexture(fluid));
    TextureAtlasSprite flowing = getBlockSprite(attributes.getFlowingTexture(fluid));
    boolean isGas = attributes.isGaseous(fluid);
    light = withBlockLight(light,attributes.getLuminosity(fluid));

    // determine height based on fluid amount
    Vector3f from = cube.getFromScaled();
    Vector3f to = cube.getToScaled();
    // gas renders upside down
    float minY = from.getY();
    float maxY = to.getY();
    float height = (fluid.getAmount() - offset) / capacity;
    if (isGas && flipGas) {
      from = from.copy();
      from.setY(maxY + (height * (minY - maxY)));
    } else {
      to = to.copy();
      to.setY(minY + (height * (maxY - minY)));
    }

    // draw cuboid
    renderCuboid(matrices, buffer.getBuffer(RENDER_TYPE), cube, still, flowing, from, to, attributes.getColor(fluid), light, isGas);
  }
}
