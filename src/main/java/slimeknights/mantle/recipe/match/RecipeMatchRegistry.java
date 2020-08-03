package slimeknights.mantle.recipe.match;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.PriorityQueue;

/**
 * Contains a set of matches. Allows you to easily find if a set of itemstacks matches one of them.
 *
 * @deprecated Reevaluating whether we still need this
 */
@SuppressWarnings("WeakerAccess")
@Deprecated
public class RecipeMatchRegistry {

  protected final PriorityQueue<RecipeMatch> items = new PriorityQueue<>(1, RecipeComparator.INSTANCE);

  // looks for a match in the given itemstacks
  public Optional<RecipeMatch.Match> matches(ItemStack... stacks) {
    NonNullList<ItemStack> nonNullStacks = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
    for (int i = 0; i < stacks.length; i++) {
      if (!stacks[i].isEmpty()) {
        nonNullStacks.set(i, stacks[i].copy());
      }
    }

    return this.matches(nonNullStacks);
  }

  // looks for a match in the given itemstacks
  public Optional<RecipeMatch.Match> matches(NonNullList<ItemStack> stacks) {
    for (RecipeMatch recipe : this.items) {
      Optional<RecipeMatch.Match> match = recipe.matches(stacks);
      if (match.isPresent()) {
        return match;
      }
    }

    return Optional.empty();
  }

  // looks for a match with at least the given amount in the given itemstacks
  public Optional<RecipeMatch.Match> matches(NonNullList<ItemStack> stacks, int minAmount) {
    stacks = copyItemStackArray(stacks); // copy so we don't modify original

    List<RecipeMatch.Match> matches = Lists.newLinkedList();

    Optional<RecipeMatch.Match> matchOptional;
    int sum = 0;
    while (sum < minAmount && (matchOptional = this.matches(stacks)).isPresent()) {
      RecipeMatch.Match match = matchOptional.get();
      matches.add(match);
      RecipeMatch.removeMatch(stacks, match);

      sum += match.amount;
    }

    // not enough found
    if (sum < minAmount) {
      return Optional.empty();
    }

    // merge all found matches into one match
    List<ItemStack> foundStacks = Lists.newLinkedList();
    for (RecipeMatch.Match m : matches) {
      foundStacks.addAll(m.stacks);
    }

    return Optional.of(new RecipeMatch.Match(foundStacks, sum));
  }

  public Optional<RecipeMatch.Match> matchesRecursively(NonNullList<ItemStack> stacks) {
    stacks = copyItemStackArray(stacks); // copy so we don't modify original

    List<RecipeMatch.Match> matches = Lists.newLinkedList();

    Optional<RecipeMatch.Match> matchOptional;
    int sum = 0;
    while ((matchOptional = this.matches(stacks)).isPresent()) {
      RecipeMatch.Match match = matchOptional.get();
      matches.add(match);
      RecipeMatch.removeMatch(stacks, match);

      sum += match.amount;
    }

    // merge all found matches into one match
    List<ItemStack> foundStacks = Lists.newLinkedList();
    for (RecipeMatch.Match m : matches) {
      foundStacks.addAll(m.stacks);
    }

    return Optional.of(new RecipeMatch.Match(foundStacks, sum));
  }

  /**
   * Associates a block with this material. Used for repairing and other.
   *
   * @param amountMatched For how many matches the block counts (e.g. redstone dust = 1 match, Redstone block = 9)
   */
  public void addItem(Block block, int amountMatched) {
    this.items.add(new RecipeMatch.Item(new ItemStack(block), 1, amountMatched));
  }

  /**
   * Associates an item entry with this material. Used for repairing and other.
   *
   * @param item          The item
   * @param amountNeeded  How many of this item are needed to count as one full material item.
   * @param amountMatched If both item and amount are present, how often did they match?
   */
  public void addItem(Item item, int amountNeeded, int amountMatched) {
    this.items.add(new RecipeMatch.Item(new ItemStack(item), amountNeeded, amountMatched));
  }

  /**
   * Associates an item entry with this material. Used for repairing and other.
   *
   * @param item          The item
   * @param amountNeeded  How many of this item are needed to count as one full material item.
   * @param amountMatched If both item and amount are present, how often did they match?
   */
  public void addItem(ItemStack item, int amountNeeded, int amountMatched) {
    this.items.add(new RecipeMatch.Item(item, amountNeeded, amountMatched));
  }

  /**
   * Associates an item with this material. Used for repairing and other.
   */
  public void addItem(Item item) {
    this.addItem(item, 1, 1);
  }

  public void addRecipeMatch(RecipeMatch match) {
    this.items.add(match);
  }

  public static NonNullList<ItemStack> copyItemStackArray(NonNullList<ItemStack> in) {
    NonNullList<ItemStack> stacksCopy = NonNullList.withSize(in.size(), ItemStack.EMPTY);
    for (int i = 0; i < in.size(); i++) {
      if (!in.get(i).isEmpty()) {
        stacksCopy.set(i, in.get(i).copy());
      }
    }

    return stacksCopy;
  }

  private static class RecipeComparator implements Comparator<RecipeMatch> {

    public static RecipeComparator INSTANCE = new RecipeComparator();

    private RecipeComparator() {
    }

    @Override
    public int compare(RecipeMatch o1, RecipeMatch o2) {
      return o2.getAmountMatched() - o1.getAmountMatched();
    }
  }
}
