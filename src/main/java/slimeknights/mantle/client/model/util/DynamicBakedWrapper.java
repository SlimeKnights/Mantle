package slimeknights.mantle.client.model.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.util.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Cross between {@link BakedModelWrapper} and {@link net.minecraftforge.client.model.data.IDynamicBakedModel}.
 * Used to create a baked model wrapper that has a dynamic {@link #getQuads(BlockState, Direction, Random, IModelData)} without worrying about overriding the deprecated variant.
 * @param <T>  Baked model parent
 */
@SuppressWarnings("WeakerAccess")
public abstract class DynamicBakedWrapper<T extends IBakedModel> extends BakedModelWrapper<T> {

  protected DynamicBakedWrapper(T originalModel) {
    super(originalModel);
  }

  @Override
  @Deprecated
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
    return this.getQuads(state, side, rand, EmptyModelData.INSTANCE);
  }

  @Override
  public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData);
}
