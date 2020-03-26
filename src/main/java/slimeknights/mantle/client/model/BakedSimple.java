package slimeknights.mantle.client.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BakedSimple implements IBakedModel {

  private final List<BakedQuad> quads;
  private final ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms;
  private final TextureAtlasSprite particle;
  private final boolean ambientOcclusion;
  private final boolean isGui3d;
  private final ItemOverrideList overrides;

  public BakedSimple(ImmutableList<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms, IBakedModel base) {
    this(
            quads,
            transforms,
            base.getParticleTexture(),
            base.isAmbientOcclusion(),
            base.isGui3d(),
            base.getOverrides()
    );
  }

  public BakedSimple(List<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms, IBakedModel base) {
    this(
            quads,
            transforms,
            base.getParticleTexture(),
            base.isAmbientOcclusion(),
            base.isGui3d(),
            base.getOverrides()
    );
  }

  public BakedSimple(List<BakedQuad> quads, ImmutableMap<ItemCameraTransforms.TransformType, TransformationMatrix> transforms, TextureAtlasSprite particle, boolean ambientOcclusion, boolean isGui3d, ItemOverrideList overrides) {
    this.quads = quads;
    this.particle = particle;
    this.transforms = transforms;
    this.ambientOcclusion = ambientOcclusion;
    this.isGui3d = isGui3d;
    this.overrides = overrides;
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
    return this.quads;
  }

  @Override
  public boolean isAmbientOcclusion() {
    return this.ambientOcclusion;
  }

  @Override
  public boolean isGui3d() {
    return this.isGui3d;
  }

  @Override
  public boolean func_230044_c_() {
    return false;
  }

  @Override
  public boolean isBuiltInRenderer() {
    return false;
  }

  @Nonnull
  @Override
  @Deprecated
  public TextureAtlasSprite getParticleTexture() {
    return this.particle;
  }

  @Nonnull
  @Override
  public TextureAtlasSprite getParticleTexture(@Nonnull IModelData data) {
    return this.particle;
  }

  @Nonnull
  @Override
  @Deprecated
  public ItemCameraTransforms getItemCameraTransforms() {
    return ItemCameraTransforms.DEFAULT;
  }

  @Nonnull
  @Override
  public ItemOverrideList getOverrides() {
    return this.overrides;
  }

  @Override
  public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
    return PerspectiveMapWrapper.handlePerspective(this, this.transforms, cameraTransformType, mat);
  }

  public static class Wrapper extends BakedSimple {

    private final IBakedModel parent;

    public Wrapper(ImmutableList<BakedQuad> quads, IBakedModel base) {
      super(quads, null, base);

      this.parent = base;
    }

    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
      this.parent.handlePerspective(cameraTransformType, mat);
      return this;
    }
  }
}
