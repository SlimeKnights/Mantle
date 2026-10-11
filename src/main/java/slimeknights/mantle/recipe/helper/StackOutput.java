package slimeknights.mantle.recipe.helper;

import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.data.loadable.field.RecordField;

import java.util.function.Supplier;

/** Shared logic for {@link ItemOutput} and {@link FluidOutput} */
public abstract class StackOutput<V,S> implements Supplier<S> {
  /** {@return the output of this recipe} Should not be modified. */
  @Override
  public abstract S get();

  /** {@return a copy of the result stack} */
  public abstract S copy();


  /* Field getters */

  /** {@return the value or the tag key based on the type} Does not resolve the stack. */
  public abstract Either<V, TagKey<V>> getValueOrTag();

  /** {@return true if the output is empty} Does not resolve the stack. */
  public abstract boolean isEmpty();

  /** {@return components for the stack} Does not resolve the stack. */
  public abstract DataComponentPatch getComponents();


  /* Codecs */

  /** Optional data components */
  protected static final MapCodec<DataComponentPatch> COMPONENTS = DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY);

  /** Starts the codec with the item and components */
  protected static <V, O extends StackOutput<V,?>> Products.P2<RecordCodecBuilder.Mu<O>, Either<V,TagKey<V>>, DataComponentPatch> codecStart(RecordCodecBuilder.Instance<O> instance, MapCodec<Either<V,TagKey<V>>> either) {
    return instance.group(either.forGetter(StackOutput::getValueOrTag), COMPONENTS.forGetter(StackOutput::getComponents));
  }


  /* Loadables */

  /** Optional data components */
  protected static final RecordField<DataComponentPatch,StackOutput<?,?>> COMPONENTS_FIELD = Loadables.DATA_COMPONENTS.defaultField("components", DataComponentPatch.EMPTY, false, StackOutput::getComponents);
}
