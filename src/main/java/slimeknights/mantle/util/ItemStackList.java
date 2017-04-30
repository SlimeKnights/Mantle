package slimeknights.mantle.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ItemStackList extends NonNullList<ItemStack> {

  protected ItemStackList() {
    this(new ArrayList<>());
  }

  protected ItemStackList(List<ItemStack> delegate) {
    super(delegate, ItemStack.EMPTY);
  }

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
  public static ItemStackList of(IInventory inventory) {
    ItemStackList itemStackList = withSize(inventory.getSizeInventory());
    for(int i = 0; i < inventory.getSizeInventory(); i++) {
      itemStackList.add(inventory.getStackInSlot(i));
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
    if(index >= 0 && index < size()) {
      set(index, ItemStack.EMPTY);
    }
  }

  /**
   * Creates a new list with the same content. ItemStacks are shared between lists!
   * @param fixed If true the list will have fixed size
   */
  public ItemStackList copy(boolean fixed) {
    ItemStackList copy = fixed ? withSize(this.size()) : create();
    for(int i = 0; i < size(); i++) {
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
    for(int i = 0; i < size(); i++) {
      copy.set(i, get(i).copy());
    }
    return copy;
  }
}
