package slimeknights.mantle.util;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Extension of {@link NonNullList} to better handle items in an inventory
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ItemStackList extends NonNullList<ItemStack> {

  /**
   * Creates a new ItemStackList with nothing
   */
  protected ItemStackList() {
    this(new ArrayList<>());
  }

  /**
   * Creates a new list from the given stacks
   * @param delegate  List of stacks
   */
  protected ItemStackList(List<ItemStack> delegate) {
    super(delegate, ItemStack.EMPTY);
  }

  /**
   * Creates an empty item stack list no size
   * @return  Empty list
   */
  public static ItemStackList create() {
    return new ItemStackList();
  }

  /**
   * Create an empty ItemStackList with the given size
   */
  public static ItemStackList withSize(int size) {
    ItemStack[] aobject = new ItemStack[size];
    Arrays.fill(aobject, ItemStack.EMPTY);
    return new ItemStackList(Arrays.asList(aobject));
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(ItemStack... element) {
    ItemStackList itemStackList = create();
    itemStackList.addAll(Arrays.asList(element));
    return itemStackList;
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(Collection<ItemStack> boringList) {
    ItemStackList itemStackList = create();
    itemStackList.addAll(boringList);
    return itemStackList;
  }

  /**
   * Create an ItemStackList from the given elements.
   */
  public static ItemStackList of(Container inventory) {
    ItemStackList itemStackList = withSize(inventory.getContainerSize());
    for (int i = 0; i < inventory.getContainerSize(); i++) {
      itemStackList.add(inventory.getItem(i));
    }
    return itemStackList;
  }

  /**
   * Checks if an Itemstack at a specific index is not empty
   *
   * @param index The index to check
   * @return true if the itemstack at index <i>index</i> is not empty, false otherwise or if the index is out of bounds
   */
  public boolean hasItem(int index) {
    return index >= 0 && index < size() && !get(index).isEmpty();
  }

  /**
   * Sets the itemstack at the given index to Itemstack.EMPTY.
   * Does nothing if the index is out of bounds.
   *
   * @param index The index to set empty
   */
  public void setEmpty(int index) {
    if (index >= 0 && index < size()) {
      set(index, ItemStack.EMPTY);
    }
  }

  /**
   * Creates a new list with the same content. ItemStacks are shared between lists!
   * @param fixed If true the list will have fixed size
   */
  public ItemStackList copy(boolean fixed) {
    ItemStackList copy = fixed ? withSize(this.size()) : create();
    for (int i = 0; i < size(); i++) {
      copy.set(i, get(i));
    }
    return copy;
  }

  /**
   * Creates a new list with the same content, but Itemstacks are copied too,
   * meaning changes to the copy will not affect the itemstacks in the original list.
   * @param fixed If true the list will have fixed size
   */
  public ItemStackList deepCopy(boolean fixed) {
    ItemStackList copy = fixed ? withSize(this.size()) : create();
    for (int i = 0; i < size(); i++) {
      copy.set(i, get(i).copy());
    }
    return copy;
  }
}
