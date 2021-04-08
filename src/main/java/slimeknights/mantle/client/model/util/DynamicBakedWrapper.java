package slimeknights.mantle.client.model.util;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.util.math.Direction;
import net.minecraftforge.client.model.BakedModelWrapper;
import slimeknights.mantle.model.EmptyModelData;
import slimeknights.mantle.model.IModelData;

import org.jetbrains.annotations.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Cross between {@link BakedModelWrapper} and {@link slimeknights.mantle.model.IDynamicBakedModel}.
 * Used to create a baked model wrapper that has a dynamic {@link #getQuads(BlockState, Direction, Random, IModelData)} without worrying about overriding the deprecated variant.
 * @param <T>  Baked model parent
 */
@SuppressWarnings("WeakerAccess")
public abstract class DynamicBakedWrapper<T extends BakedModel> extends BakedModelWrapper<T> {

  protected DynamicBakedWrapper(T originalModel) {
    super(originalModel);
  }

  /**
   * @deprecated use {@link #getQuads(BlockState, Direction, Random, IModelData)}
   */
  @Override
  @Deprecated
  public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
    return this.getQuads(state, side, rand, EmptyModelData.INSTANCE);
  }

  @Override
  public abstract List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData extraData);
}
