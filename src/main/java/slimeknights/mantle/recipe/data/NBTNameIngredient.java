package slimeknights.mantle.recipe.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

/**
 * Helper for datagen to create an NBT-sensitive ingredient referencing items from other mods by name.
 * In 1.21+, NBT has been replaced with DataComponents, and StrictNBTIngredient is replaced by
 * DataComponentIngredient.
 * 
 * This class is now a simple data holder for item name + NBT, used in datagen contexts.
 * Should never be used outside datagen.
 * 
 * @deprecated This class is being phased out. In 1.21+, use DataComponentIngredient for
 *             component-aware matching.
 */
@Deprecated(forRemoval = true)
public class NBTNameIngredient {
  private final ResourceLocation name;
  @Nullable
  private final CompoundTag nbt;

  protected NBTNameIngredient(ResourceLocation name, @Nullable CompoundTag nbt) {
    this.name = name;
    this.nbt = nbt;
  }

  /**
   * Creates an ingredient for the given name and NBT
   * @param name  Item name
   * @param nbt   NBT
   * @return  Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name, CompoundTag nbt) {
    return new NBTNameIngredient(name, nbt);
  }

  /**
   * Creates an ingredient for an item that must have no NBT
   * @param name  Item name
   * @return  Ingredient
   */
  public static NBTNameIngredient from(ResourceLocation name) {
    return new NBTNameIngredient(name, null);
  }

  /** Gets the item name */
  public ResourceLocation getName() {
    return name;
  }

  /** Gets the NBT, may be null */
  @Nullable
  public CompoundTag getNbt() {
    return nbt;
  }
}
