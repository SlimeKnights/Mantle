package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Crafting helper for common recipe types, like stairs, slabs, and packing.
 */
@SuppressWarnings("unused") // API
public interface ICommonRecipeHelper extends IRecipeHelper {
  /* Criteria helpers (mirrors RecipeProvider) */

  /** Creates a criterion for having an item. */
  default Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
    return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike));
  }

  /** Creates a criterion for having any item in the tag. */
  default Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
    return inventoryTrigger(ItemPredicate.Builder.item().of(tag));
  }

  /** Creates an inventory changed criterion. */
  default Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate.Builder... items) {
    return inventoryTrigger(Arrays.stream(items).map(ItemPredicate.Builder::build).toArray(ItemPredicate[]::new));
  }

  /** Creates an inventory changed criterion. */
  default Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
    return CriteriaTriggers.INVENTORY_CHANGED
      .createCriterion(new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(predicates)));
  }

  /* Metals */

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param category   Recipe category
   * @param large      Large item
   * @param small      Small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(RecipeOutput output, RecipeCategory category, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    ResourceLocation largeId = id(large);
    ShapedRecipeBuilder.shaped(category, large)
                       .define('#', small)
                       .pattern("###")
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_item", has(small))
                       .group(largeId.toString())
                       .save(output, wrap(largeId, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ResourceLocation smallId = id(small);
    ShapelessRecipeBuilder.shapeless(category, small, 9)
                          .requires(large)
                          .unlockedBy("has_item", has(large))
                          .group(smallId.toString())
                          .save(output, wrap(smallId, folder, String.format("_from_%s", largeName)));
  }

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param largeItem  Large item
   * @param smallItem  Small item
   * @param smallTag   Tag for small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(RecipeOutput output, RecipeCategory category, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, TagKey<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    ResourceLocation largeId = id(largeItem);
    ShapedRecipeBuilder.shaped(category, largeItem)
                       .define('#', smallTag)
                       .define('*', smallItem)
                       .pattern("###")
                       .pattern("#*#")
                       .pattern("###")
                       .unlockedBy("has_item", has(smallItem))
                       .group(largeId.toString())
                       .save(output, wrap(largeId, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ResourceLocation smallId = id(smallItem);
    ShapelessRecipeBuilder.shapeless(category, smallItem, 9)
                          .requires(largeItem)
                          .unlockedBy("has_item", has(largeItem))
                          .group(smallId.toString())
                          .save(output, wrap(smallId, folder, String.format("_from_%s", largeName)));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   * @param consumer  Recipe consumer
   * @param metal     Metal object
   * @param folder    Folder for recipes
   */
  default void metalCrafting(RecipeOutput output, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    packingRecipe(output, RecipeCategory.MISC, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    packingRecipe(output, RecipeCategory.MISC, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }


  /* Building blocks */

  /**
   * Registers generic saveing block recipes for slabs and stairs
   * @param output    Recipe output
   * @param building  Building object instance
   */
  default void slabStairsCrafting(RecipeOutput output, BuildingBlockObject building, String folder, boolean addStonecutter) {
    Item item = building.asItem();
    ResourceLocation itemId = id(item);
    Criterion<InventoryChangeTrigger.TriggerInstance> hasBlock = has(item);
    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(slab).toString())
                       .save(output, wrap(itemId, folder, "_slab"));
    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                       .define('B', item)
                       .pattern("B  ")
                       .pattern("BB ")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(stairs).toString())
                       .save(output, wrap(itemId, folder, "_stairs"));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, slab, 2)
                             .unlockedBy("has_item", hasBlock)
                             .save(output, wrap(itemId, folder, "_slab_stonecutter"));
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, stairs)
                             .unlockedBy("has_item", hasBlock)
                             .save(output, wrap(itemId, folder, "_stairs_stonecutter"));
    }
  }

  /**
   * Registers generic saveing block recipes for slabs, stairs, and walls
   * @param output    Recipe output
   * @param building  Building object instance
   */
  default void stairSlabWallCrafting(RecipeOutput output, WallBuildingBlockObject building, String folder, boolean addStonecutter) {
    slabStairsCrafting(output, building, folder, addStonecutter);
    // wall
    Item item = building.asItem();
    ResourceLocation itemId = id(item);
    Criterion<InventoryChangeTrigger.TriggerInstance> hasBlock = has(item);
    ItemLike wall = building.getWall();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wall, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(wall).toString())
                       .save(output, wrap(itemId, folder, "_wall"));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, wall)
                             .unlockedBy("has_item", hasBlock)
                             .save(output, wrap(itemId, folder, "_wall_stonecutter"));
    }
  }

  /**
   * Registers recipes relevant to wood
   * @param output    Recipe output
   * @param wood      Wood types
   * @param folder    Wood folder
   */
  default void woodCrafting(RecipeOutput output, WoodBlockObject wood, String folder) {
    Criterion<InventoryChangeTrigger.TriggerInstance> hasPlanks = has(wood);

    // planks
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, wood, 4).requires(wood.getLogItemTag())
                          .group("planks")
                          .unlockedBy("has_log", inventoryTrigger(ItemPredicate.Builder.item().of(wood.getLogItemTag())))
                          .save(output, location(folder + "planks"));
    // slab
    ItemLike slab = wood.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
                       .define('#', wood)
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_slab")
                       .save(output, location(folder + "slab"));
    // stairs
    ItemLike stairs = wood.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
                       .define('#', wood)
                       .pattern("#  ")
                       .pattern("## ")
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_stairs")
                       .save(output, location(folder + "stairs"));

    // log to stripped
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getWood(), 3)
                       .define('#', wood.getLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", has(wood.getLog()))
                       .save(output, location(folder + "log_to_wood"));
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getStrippedWood(), 3)
                       .define('#', wood.getStrippedLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", has(wood.getStrippedLog()))
                       .save(output, location(folder + "stripped_log_to_wood"));
    // doors
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, wood.getFence(), 3)
                       .define('#', Tags.Items.RODS_WOODEN).define('W', wood)
                       .pattern("W#W").pattern("W#W")
                       .group("wooden_fence")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(output, location(folder + "fence"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getFenceGate())
                       .define('#', Items.STICK).define('W', wood)
                       .pattern("#W#").pattern("#W#")
                       .group("wooden_fence_gate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(output, location(folder + "fence_gate"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getDoor(), 3)
                       .define('#', wood)
                       .pattern("##").pattern("##").pattern("##")
                       .group("wooden_door")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(output, location(folder + "door"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getTrapdoor(), 2)
                       .define('#', wood)
                       .pattern("###").pattern("###")
                       .group("wooden_trapdoor")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(output, location(folder + "trapdoor"));
    // buttons
    ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, wood.getButton())
                          .requires(wood)
                          .group("wooden_button")
                          .unlockedBy("has_planks", hasPlanks)
                          .save(output, location(folder + "button"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getPressurePlate())
                       .define('#', wood)
                       .pattern("##")
                       .group("wooden_pressure_plate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(output, location(folder + "pressure_plate"));
    // signs
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, wood.getSign(), 3)
                       .group("sign")
                       .define('#', wood).define('X', Tags.Items.RODS_WOODEN)
                       .pattern("###").pattern("###").pattern(" X ")
                       .unlockedBy("has_planks", has(wood))
                       .save(output, location(folder + "sign"));
    ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, wood.getHangingSign(), 6)
                       .group("hanging_sign")
                       .define('#', wood.getStrippedLog())
                       .define('X', Items.CHAIN)
                       .pattern("X X").pattern("###").pattern("###")
                       .unlockedBy("has_stripped_logs", has(wood.getStrippedLog()))
                       .save(output, location(folder + "hanging_sign"));
  }
}
