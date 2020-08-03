package slimeknights.mantle.recipe.match;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class that allows to find a specific subset of items in a list of itemstacks.
 * Matches can be found through tags, simple nbt-independant item-meta combinations etc.
 *
 * The match returned by this class can then be used to remove the found recipe match from the items.
 *
 * @deprecated Reevaluating whether we still need this. May be removed in 1.17
 */
@Deprecated
@SuppressWarnings("WeakerAccess")
@AllArgsConstructor
public abstract class RecipeMatch {
  /** How many of the itemstack are needed for the match */
  @Getter
  private final int amountNeeded;
  /** If amount needed itemstacks are present, as how many matches does this count? */
  @Getter
  private final int amountMatched;

  /**
   * Return all possible inputs that are a single item, if applicable.
   */
  public abstract List<ItemStack> getInputs();

  public abstract Optional<Match> matches(NonNullList<ItemStack> stacks);

  public static RecipeMatch of(net.minecraft.item.Item item) {
    return of(item, 1);
  }

  public static RecipeMatch of(net.minecraft.item.Item item, int matched) {
    return of(item, 1, matched);
  }

  public static RecipeMatch of(net.minecraft.item.Item item, int amount, int matched) {
    return new RecipeMatch.Item(new ItemStack(item), amount, matched);
  }

  public static RecipeMatch of(Block block) {
    return of(block, 1);
  }

  public static RecipeMatch of(Block block, int matched) {
    return of(block, 1, matched);
  }

  public static RecipeMatch of(Block block, int amount, int matched) {
    return new RecipeMatch.Item(new ItemStack(block), amount, matched);
  }

  public static RecipeMatch of(ItemStack stack) {
    return of(stack, 1);
  }

  public static RecipeMatch of(ItemStack stack, int matched) {
    return of(stack, 1, matched);
  }

  public static RecipeMatch of(ItemStack stack, int amount, int matched) {
    return new RecipeMatch.Item(stack.copy(), amount, matched);
  }

  public static RecipeMatch ofNBT(ItemStack stack) {
    return ofNBT(stack, 1);
  }

  public static RecipeMatch ofNBT(ItemStack stack, int matched) {
    return new RecipeMatch.ItemCombination(matched, stack);
  }

  /** Removes the match from the stacks. Has to be ensured that the match is contained in the stacks. */
  public static void removeMatch(NonNullList<ItemStack> stacks, Match match) {
    int[] amountsRemoved = new int[match.stacks.size()];

    removeOrder(stacks, match.stacks, amountsRemoved);
    removeRemaining(stacks, match.stacks, amountsRemoved);
  }

  private static void removeOrder(NonNullList<ItemStack> stacks, List<ItemStack> toRemove, int[] amountsRemoved) {
    int i = 0;
    for (int j = 0; j < amountsRemoved.length; j++) {
      ItemStack stack = toRemove.get(j);
      for (; i < stacks.size(); i++) {
        // nbt sensitive since toolparts etc. use nbt
        if (ItemStack.areItemsEqual(stack, stacks.get(i)) && ItemStack.areItemStackTagsEqual(stack, stacks.get(i))) {
          if (stacks.get(i).getCount() >= stack.getCount()) {
            stacks.get(i).shrink(stack.getCount());
            if (stacks.get(i).getCount() == 0) {
              stacks.set(i, ItemStack.EMPTY);
            }
            amountsRemoved[j] += stack.getCount();
          }
          i++;
          break;
        }
      }
    }
  }

  private static void removeRemaining(NonNullList<ItemStack> stacks, List<ItemStack> toRemove, int[] amountsRemoved) {
    for (int j = 0; j < amountsRemoved.length; j++) {
      ItemStack stack = toRemove.get(j);
      int needed = stack.getCount() - amountsRemoved[j];
      for (int i = 0; i < stacks.size() && needed > 0; i++) {
        if (ItemStack.areItemsEqual(stack, stacks.get(i)) && ItemStack.areItemStackTagsEqual(stack, stacks.get(i))) {
          int change = Math.min(stacks.get(i).getCount(), needed);
          stacks.get(i).shrink(change);
          if (stacks.get(i).getCount() == 0) {
            stacks.set(i, ItemStack.EMPTY);
          }
          needed -= change;
        }
      }
    }
  }

  /** A specific amount of a certain item is needed. Supports wildcard-metadata. Not NBT sensitive. */
  public static class Item extends RecipeMatch {

    private final ItemStack template;

    public Item(ItemStack template, int amountNeeded) {
      this(template, amountNeeded, 1);
    }

    /**
     * @param template      The itemstack to match
     * @param amountNeeded  How many of the itemstack are needed for the match
     * @param amountMatched If amountneeded itemstacks are present, as how many matches does this count?
     */
    public Item(ItemStack template, int amountNeeded, int amountMatched) {
      super(amountMatched, amountNeeded);
      this.template = template;
    }

    @Override
    public List<ItemStack> getInputs() {
      return ImmutableList.of(this.template);
    }

    @Override
    public Optional<Match> matches(NonNullList<ItemStack> stacks) {
      List<ItemStack> found = Lists.newLinkedList();
      int stillNeeded = this.getAmountNeeded();

      for (ItemStack stack : stacks) {
        if (this.template.getItem() == stack.getItem()) {
          // add the amount found to the list
          ItemStack copy = stack.copy();
          copy.setCount(Math.min(copy.getCount(), stillNeeded));
          found.add(copy);
          stillNeeded -= copy.getCount();

          // we found enough
          if (stillNeeded <= 0) {
            return Optional.of(new Match(found, this.getAmountMatched()));
          }
        }
      }

      return Optional.empty();
    }
  }

  /** A combination of multiple items. NBT SENSITIVE! */
  @SuppressWarnings("WeakerAccess")
  public static class ItemCombination extends RecipeMatch {

    protected final NonNullList<ItemStack> itemStacks;

    /**
     * @param amountMatched If amount needed itemstacks are present, as how many matches does this count?
     */
    public ItemCombination(int amountMatched, ItemStack... stacks) {
      super(amountMatched, 0);

      NonNullList<ItemStack> nonNullStacks = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
      for (int i = 0; i < stacks.length; i++) {
        if (!stacks[i].isEmpty()) {
          nonNullStacks.set(i, stacks[i].copy());
        }
      }

      this.itemStacks = nonNullStacks;
    }

    @Override
    public List<ItemStack> getInputs() {
      return ImmutableList.copyOf(this.itemStacks);
    }

    @Override
    public Optional<Match> matches(NonNullList<ItemStack> stacks) {
      List<ItemStack> found = Lists.newLinkedList();
      Set<Integer> needed = Sets.newHashSet();

      for (int i = 0; i < this.itemStacks.size(); i++) {
        if (!this.itemStacks.get(i).isEmpty()) {
          needed.add(i);
        }
      }

      for (ItemStack stack : stacks) {
        Iterator<Integer> iter = needed.iterator();
        while (iter.hasNext()) {
          int index = iter.next();
          ItemStack template = this.itemStacks.get(index);
          if (ItemStack.areItemsEqual(template, stack) && ItemStack.areItemStackTagsEqual(template, stack)) {
            // add the amount found to the list
            ItemStack copy = stack.copy();
            copy.setCount(1);
            found.add(copy);
            iter.remove();
            break;
          }
        }
      }

      if (needed.isEmpty()) {
        return Optional.of(new Match(found, this.getAmountMatched()));
      }
      return Optional.empty();
    }
  }

  /** Represents a collection of items that match the recipies */
  @SuppressWarnings("WeakerAccess")
  public static class Match {

    /** The stacks that have to be removed to apply this match */
    public List<ItemStack> stacks;

    /** How often the recipe is found within this match */
    public int amount;

    public Match(List<ItemStack> stacks, int amount) {
      this.stacks = stacks;
      this.amount = amount;
    }
  }
}
