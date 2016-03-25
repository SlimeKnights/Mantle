package slimeknights.mantle.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

import javax.vecmath.Matrix4f;

import slimeknights.mantle.client.ModelHelper;

/**
 * Takes a blockmodel and applies the standard-block-perspective for third_person to it
 */
public class BlockItemModelWrapper implements IPerspectiveAwareModel, IBakedModel {

  private final IBakedModel parent;

  public BlockItemModelWrapper(IBakedModel parent) {
    this.parent = parent;
  }

  @Override
  public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
    Matrix4f matrix = null;
    // fix transformation in hand
    if(cameraTransformType == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
      matrix = ModelHelper.BLOCK_THIRD_PERSON_RIGHT.getMatrix();
    }
    else if(cameraTransformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
      matrix = ModelHelper.BLOCK_THIRD_PERSON_LEFT.getMatrix();
    }

    return Pair.of(this, matrix);
  }

  @Override
  public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
    return parent.getQuads(state, side, rand);
  }

  @Override
  public boolean isAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return parent.isGui3d();
  }

  @Override
  public boolean isBuiltInRenderer() {
    return parent.isBuiltInRenderer();
  }

  @Override
  public TextureAtlasSprite getParticleTexture() {
    return parent.getParticleTexture();
  }

  @Override
  public ItemCameraTransforms getItemCameraTransforms() {
    return parent.getItemCameraTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return null;
  }
}
