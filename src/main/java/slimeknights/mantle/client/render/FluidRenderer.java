package slimeknights.mantle.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Matrix4f;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.client.model.fluid.FluidCuboid;

import java.util.List;

@SuppressWarnings({"WeakerAccess", "unused"})
public class FluidRenderer {
  /** Render type used for rendering fluids */
  public static final RenderLayer RENDER_TYPE = RenderLayer.of(
      Mantle.modId + ":block_render_type",
      VertexFormats.POSITION_COLOR_TEXTURE_LIGHT, 7, 256, true, false,
      RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, false, false))
                      .shadeModel(RenderLayer.SMOOTH_SHADE_MODEL)
                      .lightmap(RenderLayer.ENABLE_LIGHTMAP)
                      .texture(RenderLayer.MIPMAP_BLOCK_ATLAS_TEXTURE)
                      .transparency(RenderLayer.TRANSLUCENT_TRANSPARENCY)
                      .build(false));

  /**
   * Gets a block sprite from the given location
   * @param sprite  Sprite name
   * @return  Sprite location
   */
  public static Sprite getBlockSprite(Identifier sprite) {
    return MinecraftClient.getInstance().getBakedModelManager().method_24153(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE).getSprite(sprite);
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
  public static void putTexturedQuad(VertexConsumer renderer, Matrix4f matrix, Sprite sprite, Vector3f from, Vector3f to, Direction face, int color, int brightness, int rotation, boolean flowing) {
    // start with texture coordinates
    float x1 = from.getX(), y1 = from.getY(), z1 = from.getZ();
    float x2 = to.getX(), y2 = to.getY(), z2 = to.getZ();
    // choose UV based on opposite two axis
    float u1, u2, v1, v2;
    switch (face.getAxis()) {
      case Y:
      default:
        u1 = x1; u2 = x2;
        v1 = z2; v2 = z1;
        break;
      case Z:
        u1 = x2; u2 = x1;
        v1 = y1; v2 = y2;
        break;
      case X:
        u1 = z2; u2 = z1;
        v1 = y1; v2 = y2;
        break;
    }

    // wrap UV to be between 0 and 1, assumes none of the positions lie outside the 0,0,0 to 1,1,1 range
    // however, one of them might be exactly on the 1.0 bound, that one should be set to 1 instead of left at 0
    boolean bigger = u1 > u2;
    u1 = u1 % 1;
    u2 = u2 % 1;
    if (bigger) {
      if (u1 == 0) u1 = 1;
    } else {
      if (u2 == 0) u2 = 1;
    }
    bigger = v1 > v2;
    v1 = v1 % 1;
    v2 = v2 % 1;
    if (bigger) {
      if (v1 == 0) v1 = 1;
    } else {
      if (v2 == 0) v2 = 1;
    }

    // flip V when relevant
    if (rotation == 0 || rotation == 270) {
      float temp = v1;
      v1 = 1f - v2;
      v2 = 1f - temp;
    }
    // flip U when relevant
    if (rotation >= 180) {
      float temp = u1;
      u1 = 1f - u2;
      u2 = 1f - temp;
    }
    // if rotating by 90 or 270, swap U and V
    float minU, maxU, minV, maxV;
    double size = flowing ? 8 : 16;
    if ((rotation % 180) == 90) {
      minU = sprite.getFrameU(v1 * size);
      maxU = sprite.getFrameU(v2 * size);
      minV = sprite.getFrameV(u1 * size);
      maxV = sprite.getFrameV(u2 * size);
    } else {
      minU = sprite.getFrameU(u1 * size);
      maxU = sprite.getFrameU(u2 * size);
      minV = sprite.getFrameV(v1 * size);
      maxV = sprite.getFrameV(v2 * size);
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
        renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
        break;
      case UP:
        renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
        break;
      case NORTH:
        renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
        break;
      case SOUTH:
        renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
        break;
      case WEST:
        renderer.vertex(matrix, x1, y1, z2).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x1, y2, z2).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x1, y2, z1).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x1, y1, z1).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
        break;
      case EAST:
        renderer.vertex(matrix, x2, y1, z1).color(r, g, b, a).texture(u1, v1).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z1).color(r, g, b, a).texture(u2, v2).light(light1, light2).next();
        renderer.vertex(matrix, x2, y2, z2).color(r, g, b, a).texture(u3, v3).light(light1, light2).next();
        renderer.vertex(matrix, x2, y1, z2).color(r, g, b, a).texture(u4, v4).light(light1, light2).next();
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
  public static void renderCuboid(MatrixStack matrices, VertexConsumer buffer, FluidCuboid cube, Sprite still, Sprite flowing, Vector3f from, Vector3f to, int color, int light, boolean isGas) {
//    Matrix4f matrix = matrices.peek().getModel();
//    int rotation = isGas ? 180 : 0;
//    for (Direction dir : Direction.values()) {
//      FluidFace face = cube.getFace(dir);
//      if (face != null) {
//        boolean isFlowing = face.isFlowing();
//        int faceRot = (rotation + face.getRotation()) % 360;
//        putTexturedQuad(buffer, matrix, isFlowing ? flowing : still, from, to, dir, color, light, faceRot, isFlowing);
//      }
//    }
  }

  /**
   * Renders a list of fluid cuboids
   * @param matrices  Matrix stack instance
   * @param buffer    Buffer instance
   * @param cubes     List of cubes to render
   * @param fluid     Fluid to use in rendering
   * @param light     Light level from TER
   */
  public static void renderCuboids(MatrixStack matrices, VertexConsumer buffer, List<FluidCuboid> cubes, FluidStack fluid, int light) {
//    if (fluid.isEmpty()) {
//      return;
//    }
//
//    // fluid attributes, fetch once for all fluids to save effort
//    FluidAttributes attributes = fluid.getFluid().getAttributes();
//    Sprite still = getBlockSprite(attributes.getStillTexture(fluid));
//    Sprite flowing = getBlockSprite(attributes.getFlowingTexture(fluid));
//    int color = attributes.getColor(fluid);
//    light = withBlockLight(light,attributes.getLuminosity(fluid));
//    boolean isGas = attributes.isGaseous(fluid);
//
//    // render all given cuboids
//    for (FluidCuboid cube : cubes) {
//      renderCuboid(matrices, buffer, cube, still, flowing, cube.getFromScaled(), cube.getToScaled(), color, light, isGas);
//    }
  }

  /**
   * Renders a fluid cuboid with the given offset, used to manually place cuboids from a list for rendering {@link #renderCuboids(MatrixStack, VertexConsumer, List, FluidStack, int)}
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
  public static void renderCuboid(MatrixStack matrices, VertexConsumer buffer, FluidCuboid cube, float yOffset, Sprite still, Sprite flowing, int color, int light, boolean isGas) {
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
  public static void renderScaledCuboid(MatrixStack matrices, VertexConsumerProvider buffer, FluidCuboid cube, FluidStack fluid, float offset, int capacity, int light, boolean flipGas) {
    // nothing to render
//    if (fluid.isEmpty() || capacity <= 0) {
//      return;
//    }
//
//    // fluid attributes
//    FluidAttributes attributes = fluid.getFluid().getAttributes();
//    Sprite still = getBlockSprite(attributes.getStillTexture(fluid));
//    Sprite flowing = getBlockSprite(attributes.getFlowingTexture(fluid));
//    boolean isGas = attributes.isGaseous(fluid);
//    light = withBlockLight(light,attributes.getLuminosity(fluid));
//
//    // determine height based on fluid amount
//    Vector3f from = cube.getFromScaled();
//    Vector3f to = cube.getToScaled();
//    // gas renders upside down
//    float minY = from.getY();
//    float maxY = to.getY();
//    float height = (fluid.getAmount() - offset) / capacity;
//    if (isGas && flipGas) {
//      from = from.copy();
//      from.setY(maxY + (height * (minY - maxY)));
//    } else {
//      to = to.copy();
//      to.setY(minY + (height * (maxY - minY)));
//    }
//
//    // draw cuboid
//    renderCuboid(matrices, buffer.getBuffer(RENDER_TYPE), cube, still, flowing, from, to, attributes.getColor(fluid), light, isGas);
  }
}
