package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BakedWrapper implements IBakedModel {

  protected final IBakedModel parent;

  public BakedWrapper(IBakedModel parent) {
    this.parent = parent;
  }

  @Override
  @Deprecated
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
    return this.getQuads(state, side, rand, EmptyModelData.INSTANCE);
  }

  @Override
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData) {
    return this.parent.getQuads(state, side, rand, extraData);
  }

  @Override
  public boolean isAmbientOcclusion() {
    return this.parent.isAmbientOcclusion();
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

  @Override
  @Deprecated
  public TextureAtlasSprite getParticleTexture() {
    return this.parent.getParticleTexture();
  }

  @Override
  public TextureAtlasSprite getParticleTexture(IModelData data) {
    return this.parent.getParticleTexture(data);
  }

  @Override
  @Deprecated
  public ItemCameraTransforms getItemCameraTransforms() {
    return this.parent.getItemCameraTransforms();
  }

  @Override
  public ItemOverrideList getOverrides() {
    return this.parent.getOverrides();
  }

  @Override
  public IModelData getModelData(IBlockDisplayReader world, BlockPos pos, BlockState state, IModelData tileData) {
    return this.parent.getModelData(world, pos, state, tileData);
  }

  public static class Perspective extends BakedWrapper implements IBakedModel {

    protected final ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms;

    public Perspective(IBakedModel parent, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms) {
      super(parent);
      this.transforms = transforms;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
      return PerspectiveMapWrapper.handlePerspective(this, this.transforms, cameraTransformType, mat);
    }

    @Override
    public boolean func_230044_c_() {
      return false;
    }
  }
}
