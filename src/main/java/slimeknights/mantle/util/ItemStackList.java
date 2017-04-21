package slimeknights.mantle.util;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemStackList extends NonNullList<ItemStack> {

  protected ItemStackList() {
    this(new ArrayList<>());
  }

  protected ItemStackList(List<ItemStack> delegate) {
    super(new ArrayList<>(), ItemStack.EMPTY);
  }

  public static ItemStackList create() {
    return new ItemStackList();
  }

  /**
   * Create an empty ItemStackList with the given size
   */
  public static ItemStackList withSize(int size) {
    return new ItemStackList(IntStream.range(0, size).mapToObj(i -> ItemStack.EMPTY).collect(Collectors.toList()));
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
}
