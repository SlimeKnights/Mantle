package slimeknights.mantle.registration;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Helpers for registering things */
public class RegistrationHelper {
  private RegistrationHelper() {}

  /** Properties for a standard bucket item */
  public static final Item.Properties BUCKET_PROPS = new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1);

  /** Casts the class type to make it a valid argument type */
  @SuppressWarnings("unchecked")
  public static <T extends ArgumentType<?>> Class<T> genericArgumentType(Class<? super T> type) {
    return (Class<T>) type;
  }


  /* Wood types */

  /** Wood types to register with the texture atlas */
  private static final List<WoodType> WOOD_TYPES = new ArrayList<>();

  /** Registers a wood type to be injected into the atlas, should be called before client setup */
  public static void registerWoodType(WoodType type) {
    synchronized (WOOD_TYPES) {
      WOOD_TYPES.add(type);
      WoodType.register(type);
    }
  }

  /** Runs the given consumer for each wood type registered */
  public static void forEachWoodType(Consumer<WoodType> consumer) {
    WOOD_TYPES.forEach(consumer);
  }


  /* Sign blocks */

  /** Sign blocks to use for the block entity valid blocks */
  private static final List<Supplier<? extends Block>> SIGN_BLOCKS = new ArrayList<>();
  /** Hanging sign blocks to use for the block entity valid blocks */
  private static final List<Supplier<? extends Block>> HANGING_SIGN_BLOCKS = new ArrayList<>();

  /**
   * Registers a sign block to be injected into the tile entity, should be called during registration
   * @param sign  Sign block supplier
   */
  public static void registerSignBlock(Supplier<? extends Block> sign) {
    synchronized (SIGN_BLOCKS) {
      SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of sign blocks for TE registration */
  public static Block[] buildSignBlocks() {
    return SIGN_BLOCKS.stream().map(Supplier::get).toArray(Block[]::new);
  }

  /**
   * Registers a sign block to be injected into the tile entity, should be called during registration
   * @param sign  Sign block supplier
   */
  public static void registerHangingSignBlock(Supplier<? extends Block> sign) {
    synchronized (HANGING_SIGN_BLOCKS) {
      HANGING_SIGN_BLOCKS.add(sign);
    }
  }

  /** Builds the list of sign blocks for TE registration */
  public static Block[] buildHangingSignBlocks() {
    return HANGING_SIGN_BLOCKS.stream().map(Supplier::get).toArray(Block[]::new);
  }
}
