package slimeknights.mantle.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import slimeknights.mantle.Mantle;

/**
 * Utility class that allows to find a specific subset of items in a list of itemstacks.
 * Matches can be found through oredictionary, simple nbt-independant item-meta combinations etc.
 *
 * The match returned by this class can then be used to remove the found recipe match from the items.
 */
public abstract class RecipeMatch {

  public final int amountNeeded;
  public final int amountMatched;

  /**
   * @param amountNeeded  How many of the itemstack are needed for the match
   * @param amountMatched If amount needed itemstacks are present, as how many matches does this count?
   */
  public RecipeMatch(int amountMatched, int amountNeeded) {
    this.amountMatched = amountMatched;
    this.amountNeeded = amountNeeded;
  }

  /**
   * Return all possible inputs that are a single item, if applicable.
   */
  public abstract List<ItemStack> getInputs();

  public abstract Match matches(ItemStack[] stacks);

  public static RecipeMatch of(String oredict) {
    return of(oredict, 1);
  }

  public static RecipeMatch of(String oredict, int matched) {
    return of(oredict, 1, matched);
  }

  public static RecipeMatch of(String oredict, int amount, int matched) {
    return new RecipeMatch.Oredict(oredict, amount, matched);
  }

  public static RecipeMatch of(List<ItemStack> oredict) {
    return of(oredict, 1);
  }

  public static RecipeMatch of(List<ItemStack> oredict, int matched) {
    return of(oredict, 1, matched);
  }

  public static RecipeMatch of(List<ItemStack> oredict, int amount, int matched) {
    return new RecipeMatch.Oredict(oredict, amount, matched);
  }

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

  /** Removes the match from the stacks */
  public static void removeMatch(ItemStack[] stacks, Match match) {
    for(ItemStack stack : match.stacks) {
      for(int i = 0; i < stacks.length; i++) {
        // nbt sensitive since toolparts etc. use nbt
        if(ItemStack.areItemsEqual(stack, stacks[i]) && ItemStack.areItemStackTagsEqual(stack, stacks[i])) {
          if(stacks[i].stackSize < stack.stackSize) {
            Mantle.logger.error("RecipeMatch has incorrect stacksize! {}", stacks[i].toString());
          }
          else {
            stacks[i].stackSize -= stack.stackSize;
            if(stacks[i].stackSize == 0) {
              stacks[i] = null;
            }
          }
          break;
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
      return ImmutableList.of(template);
    }

    @Override
    public Match matches(ItemStack[] stacks) {
      List<ItemStack> found = Lists.newLinkedList();
      int stillNeeded = amountNeeded;

      for(ItemStack stack : stacks) {
        if(OreDictionary.itemMatches(template, stack, false)) {
          // add the amount found to the list
          ItemStack copy = stack.copy();
          copy.stackSize = Math.min(copy.stackSize, stillNeeded);
          found.add(copy);
          stillNeeded -= copy.stackSize;

          // we found enough
          if(stillNeeded <= 0) {
            return new Match(found, amountMatched);
          }
        }
      }

      return null;
    }
  }

  /** A combination of multiple items. NBT SENSITIVE! */
  public static class ItemCombination extends RecipeMatch {

    protected final ItemStack[] itemStacks;

    /**
     * @param amountMatched If amount needed itemstacks are present, as how many matches does this count?
     */
    public ItemCombination(int amountMatched, ItemStack... stacks) {
      super(amountMatched, 0);

      this.itemStacks = stacks;
    }

    @Override
    public List<ItemStack> getInputs() {
      return ImmutableList.copyOf(itemStacks);
    }

    @Override
    public Match matches(ItemStack[] stacks) {
      List<ItemStack> found = Lists.newLinkedList();
      Set<Integer> needed = Sets.newHashSet();

      for(int i = 0; i < itemStacks.length; i++) {
        if(itemStacks[i] != null) {
          needed.add(i);
        }
      }

      for(ItemStack stack : stacks) {
        Iterator<Integer> iter = needed.iterator();
        while(iter.hasNext()) {
          int index = iter.next();
          ItemStack template = itemStacks[index];
          if(ItemStack.areItemsEqual(template, stack) && ItemStack.areItemStackTagsEqual(template, stack)) {
            // add the amount found to the list
            ItemStack copy = stack.copy();
            copy.stackSize = 1;
            found.add(copy);
            iter.remove();
            break;
          }
        }
      }

      if(needed.isEmpty()) {
        return new Match(found, amountMatched);
      }
      return null;
    }
  }

  /** A specific amount of an oredicted material is needed to match. */
  public static class Oredict extends RecipeMatch {

    private final List<ItemStack> oredictEntry; // todo: change this to the actual list in the oredict

    public Oredict(List<ItemStack> oredictEntry, int amountNeeded) {
      this(oredictEntry, amountNeeded, 1);
    }

    public Oredict(List<ItemStack> oredictEntry, int amountNeeded, int amountMatched) {
      super(amountMatched, amountNeeded);
      this.oredictEntry = oredictEntry;
    }

    public Oredict(String oredictEntry, int amountNeeded) {
      this(oredictEntry, amountNeeded, 1);
    }

    public Oredict(String oredictEntry, int amountNeeded, int amountMatched) {
      super(amountMatched, amountNeeded);
      this.oredictEntry = OreDictionary.getOres(oredictEntry);
    }

    @Override
    public List<ItemStack> getInputs() {
      return oredictEntry;
    }

    @Override
    public Match matches(ItemStack[] stacks) {
      List<ItemStack> found = Lists.newLinkedList();
      int stillNeeded = amountNeeded;

      for(ItemStack ore : oredictEntry) {
        for(ItemStack stack : stacks) {
          if(OreDictionary.itemMatches(ore, stack, false)) {
            // add the amount found to the list
            ItemStack copy = stack.copy();
            copy.stackSize = Math.min(copy.stackSize, stillNeeded);
            found.add(copy);
            stillNeeded -= copy.stackSize;

            // we found enough
            if(stillNeeded <= 0) {
              return new Match(found, amountMatched);
            }
          }
        }
      }

      return null;
    }
  }

  /** Represents a collection of items that match the recipies */
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
