package slimeknights.mantle.plugin.jei.entity;

import com.mojang.datafixers.util.Pair;
import mezz.jei.api.constants.Tags;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeSpawnEggItem;
import slimeknights.mantle.data.loadable.Loadables;
import slimeknights.mantle.plugin.jei.MantleJEIConstants;
import slimeknights.mantle.recipe.ingredient.EntityIngredient.EntityInput;
import slimeknights.mantle.util.RegistryHelper;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Handler for working with entity types as ingredients */
public class EntityIngredientHelper implements IIngredientHelper<EntityInput> {
  private final TagKey<EntityType<?>> hiddenFromRecipeViewers;

  public EntityIngredientHelper() {
    hiddenFromRecipeViewers = TagKey.create(Registries.ENTITY_TYPE, Tags.HIDDEN_FROM_RECIPE_VIEWERS);
  }


  @Override
  public IIngredientType<EntityInput> getIngredientType() {
    return MantleJEIConstants.ENTITY_TYPE;
  }

  @Override
  public String getDisplayName(EntityInput type) {
    return type.type().getDescription().getString();
  }


  /* IDs */

  @Override
  public String getUniqueId(EntityInput type, UidContext context) {
    return getResourceLocation(type).toString();
  }

  @Override
  public ResourceLocation getResourceLocation(EntityInput type) {
    return Loadables.ENTITY_TYPE.getKey(type.type());
  }

  @Override
  public String getErrorInfo(@Nullable EntityInput type) {
    if (type == null) {
      return "null";
    }
    return getResourceLocation(type).toString();
  }


  /* Ingredient */

  @Override
  public ItemStack getCheatItemStack(EntityInput ingredient) {
    Item egg = ForgeSpawnEggItem.fromEntityType(ingredient.type());
    if (egg != null) {
      return new ItemStack(egg);
    }
    return ItemStack.EMPTY;
  }

  @Override
  public EntityInput copyIngredient(EntityInput type) {
    return type;
  }


  /* Tags */

  @SuppressWarnings("deprecation")
  @Override
  public Stream<ResourceLocation> getTagStream(EntityInput ingredient) {
    return ingredient.type().builtInRegistryHolder().tags().map(TagKey::location);
  }

  @Override
  public boolean isHiddenFromRecipeViewersByTags(EntityInput ingredient) {
    return ingredient.type().is(hiddenFromRecipeViewers);
  }

  @Override
  public Optional<TagKey<?>> getTagKeyEquivalent(Collection<EntityInput> ingredients) {
    List<EntityType<?>> types = ingredients.stream().map(EntityInput::type).collect(Collectors.toList());
    return BuiltInRegistries.ENTITY_TYPE.getTags()
      .filter(entry -> RegistryHelper.isTagEquivalent(entry.getSecond(), types))
      .<TagKey<?>>map(Pair::getFirst).findFirst();
  }
}
