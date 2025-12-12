package slimeknights.mantle.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.neoforged.neoforge.common.loot.LootModifierManager;
import slimeknights.mantle.data.JsonCodec.GsonCodec;

/** This class contains codecs for various vanilla things that we need to use in codecs. Typically the reason is forge pre-emptively moved a thing to codecs before vanilla did. */
public class MantleCodecs {
  /** Codec for loot pool entries */
  public static final Codec<LootPoolEntryContainer> LOOT_ENTRY = new GsonCodec<>("loot entry", LootModifierManager.GSON_INSTANCE, LootPoolEntryContainer.class);
  /** Codec for loot pool entries */
  public static final Codec<LootItemFunction[]> LOOT_FUNCTIONS = new GsonCodec<>("loot functions", LootModifierManager.GSON_INSTANCE, LootItemFunction[].class);
  /** Codec for ingredients, handling forge ingredient types */
  public static final Codec<Ingredient> INGREDIENT = new JsonCodec<>() {
    @Override
    public Ingredient deserialize(JsonElement element, DynamicOps<?> ops) {
      return Ingredient.fromJson(element);
    }

    @Override
    public JsonElement serialize(Ingredient ingredient, DynamicOps<?> ops) {
      return ingredient.toJson();
    }

    @Override
    public String toString() {
      return "Ingredient";
    }
  };
}
