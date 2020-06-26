package slimeknights.mantle.client.model;


import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import slimeknights.mantle.client.ModelHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Takes a blockmodel and applies the standard-block-perspective for third_person to it
 */
public class BlockItemModelWrapper implements IBakedModel {

  private final IBakedModel parent;

  public BlockItemModelWrapper(IBakedModel parent) {
    this.parent = parent;
  }

  @Override
  public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
    TransformationMatrix transform = null;
    // fix transformation in hand
    if (cameraTransformType == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND) {
      transform = ModelHelper.BLOCK_THIRD_PERSON_RIGHT;
    }
    else if (cameraTransformType == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND) {
      transform = ModelHelper.BLOCK_THIRD_PERSON_LEFT;
    }

    if (transform != null) {
      mat.getLast().getMatrix().mul(transform.getMatrix());
    }
    return this;
  }

  @Nonnull
  @Override
  @Deprecated
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand) {
    return this.getQuads(state, side, rand, EmptyModelData.INSTANCE);
  }

  @Nonnull
  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
    return this.parent.getQuads(state, side, rand, extraData);
  }

  @Override
  public boolean isAmbientOcclusion() {
    return false;
  }

  @Override
  public boolean isGui3d() {
    return this.parent.isGui3d();
  }

  @Override
  public boolean func_230044_c_() {
    return false;
  }

  @Override
  public boolean isBuiltInRenderer() {
    return this.parent.isBuiltInRenderer();
  }

  @Nonnull
  @Override
  @Deprecated
  public TextureAtlasSprite getParticleTexture() {
    return this.parent.getParticleTexture();
  }

  @Nonnull
  @Override
  public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
    return this.parent.getParticleTexture(data);
  }

  @Nonnull
  @Override
  public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData) {
    return this.parent.getModelData(world, pos, state, tileData);
  }

  @Nonnull
  @Override
  @Deprecated
  public ItemCameraTransforms getItemCameraTransforms() {
    return this.parent.getItemCameraTransforms();
  }

  @Nonnull
  @Override
  public ItemOverrideList getOverrides() {
    return ItemOverrideList.EMPTY;
  }
}
