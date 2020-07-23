package slimeknights.mantle.registration.object;

import lombok.AllArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object that represents a block with an item form, allows fetching either form, and implements {@link IItemProvider}
 * @param <B>  Block class
 */
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor
public class BlockItemObject<B extends Block> implements Supplier<B>, IItemProvider {
  protected final Supplier<? extends B> block;

  /**
   * Creates a block item object based on a block instance
   * @param block  Block instance
   * @param <B>    Return type
   * @return  Block Item object instance
   */
  @SuppressWarnings("unchecked")
  public static <B extends Block> BlockItemObject<B> fromBlock(B block) {
    IRegistryDelegate<Block> delegate = block.delegate;
    return new BlockItemObject<>(() -> (B)delegate.get());
  }

  /**
   * Gets the block for this object
   * @return  Block
   */
  @Override
  public B get() {
    return block.get();
  }

  @Override
  public Item asItem() {
    return block.get().asItem();
  }

  /**
   * Gets the resource location for the given block
   * @return  Resource location for the given block
   */
  public ResourceLocation getRegistryName() {
    return Objects.requireNonNull(block.get().getRegistryName());
  }
}
