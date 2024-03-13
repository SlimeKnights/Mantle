package slimeknights.mantle.recipe.data;

import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

import java.util.function.Consumer;

/**
 * Crafting helper for common recipe types, like stairs, slabs, and packing.
 */
@SuppressWarnings("unused") // API
public interface ICommonRecipeHelper extends IRecipeHelper {
  /* Metals */

  /**
   * Registers a recipe packing a small item into a large one
   * @param consumer   Recipe consumer
   * @param large      Large item
   * @param small      Small item
   * @param largeName  Large name
   * @param smallName  Small name
   * @param folder     Recipe folder
   */
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    ResourceLocation largeId = id(large);
    ShapedRecipeBuilder.shaped(large)
                       .define('#', small)
                       .pattern("###")
                       .pattern("###")
                       .pattern("###")
                       .unlockedBy("has_item", RecipeProvider.has(small))
                       .group(largeId.toString())
                       .save(consumer, wrap(largeId, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ResourceLocation smallId = id(small);
    ShapelessRecipeBuilder.shapeless(small, 9)
                          .requires(large)
                          .unlockedBy("has_item", RecipeProvider.has(large))
                          .group(smallId.toString())
                          .save(consumer, wrap(smallId, folder, String.format("_from_%s", largeName)));
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
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, TagKey<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    ResourceLocation largeId = id(largeItem);
    ShapedRecipeBuilder.shaped(largeItem)
                       .define('#', smallTag)
                       .define('*', smallItem)
                       .pattern("###")
                       .pattern("#*#")
                       .pattern("###")
                       .unlockedBy("has_item", RecipeProvider.has(smallItem))
                       .group(largeId.toString())
                       .save(consumer, wrap(largeId, folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ResourceLocation smallId = id(smallItem);
    ShapelessRecipeBuilder.shapeless(smallItem, 9)
                          .requires(largeItem)
                          .unlockedBy("has_item", RecipeProvider.has(largeItem))
                          .group(smallId.toString())
                          .save(consumer, wrap(smallId, folder, String.format("_from_%s", largeName)));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   * @param consumer  Recipe consumer
   * @param metal     Metal object
   * @param folder    Folder for recipes
   */
  default void metalCrafting(Consumer<FinishedRecipe> consumer, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    packingRecipe(consumer, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    packingRecipe(consumer, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }


  /* Building blocks */

  /**
   * Registers generic saveing block recipes for slabs and stairs
   * @param consumer  Recipe consumer
   * @param building  Building object instance
   */
  default void slabStairsCrafting(Consumer<FinishedRecipe> consumer, BuildingBlockObject building, String folder, boolean addStonecutter) {
    Item item = building.asItem();
    ResourceLocation itemId = id(item);
    TriggerInstance hasBlock = RecipeProvider.has(item);
    // slab
    ItemLike slab = building.getSlab();
    ShapedRecipeBuilder.shaped(slab, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(slab).toString())
                       .save(consumer, wrap(itemId, folder, "_slab"));
    // stairs
    ItemLike stairs = building.getStairs();
    ShapedRecipeBuilder.shaped(stairs, 4)
                       .define('B', item)
                       .pattern("B  ")
                       .pattern("BB ")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(stairs).toString())
                       .save(consumer, wrap(itemId, folder, "_stairs"));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, slab, 2)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, wrap(itemId, folder, "_slab_stonecutter"));
      SingleItemRecipeBuilder.stonecutting(ingredient, stairs)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, wrap(itemId, folder, "_stairs_stonecutter"));
    }
  }

  /**
   * Registers generic saveing block recipes for slabs, stairs, and walls
   * @param consumer  Recipe consumer
   * @param saveing  Building object instance
   */
  default void stairSlabWallCrafting(Consumer<FinishedRecipe> consumer, WallBuildingBlockObject saveing, String folder, boolean addStonecutter) {
    slabStairsCrafting(consumer, saveing, folder, addStonecutter);
    // wall
    Item item = saveing.asItem();
    ResourceLocation itemId = id(item);
    TriggerInstance hasBlock = RecipeProvider.has(item);
    ItemLike wall = saveing.getWall();
    ShapedRecipeBuilder.shaped(wall, 6)
                       .define('B', item)
                       .pattern("BBB")
                       .pattern("BBB")
                       .unlockedBy("has_item", hasBlock)
                       .group(id(wall).toString())
                       .save(consumer, wrap(itemId, folder, "_wall"));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, wall)
                             .unlockedBy("has_item", hasBlock)
                             .save(consumer, wrap(itemId, folder, "_wall_stonecutter"));
    }
  }

  /**
   * Registers recipes relevant to wood
   * @param consumer  Recipe consumer
   * @param wood      Wood types
   * @param folder    Wood folder
   */
  default void woodCrafting(Consumer<FinishedRecipe> consumer, WoodBlockObject wood, String folder) {
    TriggerInstance hasPlanks = RecipeProvider.has(wood);

    // planks
    ShapelessRecipeBuilder.shapeless(wood, 4).requires(wood.getLogItemTag())
                          .group("planks")
                          .unlockedBy("has_log", RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(wood.getLogItemTag()).build()))
                          .save(consumer, location(folder + "planks"));
    // slab
    ItemLike slab = wood.getSlab();
    ShapedRecipeBuilder.shaped(slab, 6)
                       .define('#', wood)
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_slab")
                       .save(consumer, location(folder + "slab"));
    // stairs
    ItemLike stairs = wood.getStairs();
    ShapedRecipeBuilder.shaped(stairs, 4)
                       .define('#', wood)
                       .pattern("#  ")
                       .pattern("## ")
                       .pattern("###")
                       .unlockedBy("has_planks", hasPlanks)
                       .group("wooden_stairs")
                       .save(consumer, location(folder + "stairs"));

    // log to stripped
    ShapedRecipeBuilder.shaped(wood.getWood(), 3)
                       .define('#', wood.getLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", RecipeProvider.has(wood.getLog()))
                       .save(consumer, location(folder + "log_to_wood"));
    ShapedRecipeBuilder.shaped(wood.getStrippedWood(), 3)
                       .define('#', wood.getStrippedLog())
                       .pattern("##").pattern("##")
                       .group("bark")
                       .unlockedBy("has_log", RecipeProvider.has(wood.getStrippedLog()))
                       .save(consumer, location(folder + "stripped_log_to_wood"));
    // doors
    ShapedRecipeBuilder.shaped(wood.getFence(), 3)
                       .define('#', Tags.Items.RODS_WOODEN).define('W', wood)
                       .pattern("W#W").pattern("W#W")
                       .group("wooden_fence")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, location(folder + "fence"));
    ShapedRecipeBuilder.shaped(wood.getFenceGate())
                       .define('#', Items.STICK).define('W', wood)
                       .pattern("#W#").pattern("#W#")
                       .group("wooden_fence_gate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, location(folder + "fence_gate"));
    ShapedRecipeBuilder.shaped(wood.getDoor(), 3)
                       .define('#', wood)
                       .pattern("##").pattern("##").pattern("##")
                       .group("wooden_door")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, location(folder + "door"));
    ShapedRecipeBuilder.shaped(wood.getTrapdoor(), 2)
                       .define('#', wood)
                       .pattern("###").pattern("###")
                       .group("wooden_trapdoor")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, location(folder + "trapdoor"));
    // buttons
    ShapelessRecipeBuilder.shapeless(wood.getButton())
                          .requires(wood)
                          .group("wooden_button")
                          .unlockedBy("has_planks", hasPlanks)
                          .save(consumer, location(folder + "button"));
    ShapedRecipeBuilder.shaped(wood.getPressurePlate())
                       .define('#', wood)
                       .pattern("##")
                       .group("wooden_pressure_plate")
                       .unlockedBy("has_planks", hasPlanks)
                       .save(consumer, location(folder + "pressure_plate"));
    // signs
    ShapedRecipeBuilder.shaped(wood.getSign(), 3)
                       .group("sign")
                       .define('#', wood).define('X', Tags.Items.RODS_WOODEN)
                       .pattern("###").pattern("###").pattern(" X ")
                       .unlockedBy("has_planks", RecipeProvider.has(wood))
                       .save(consumer, location(folder + "sign"));

  }
}
