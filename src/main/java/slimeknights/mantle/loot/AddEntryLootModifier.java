package slimeknights.mantle.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.loot.builder.AbstractLootModifierBuilder;
import slimeknights.mantle.loot.condition.EmptyModifierLootCondition;
import slimeknights.mantle.loot.condition.ILootModifierCondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table */
public class AddEntryLootModifier extends LootModifier {
	static final Gson GSON = Deserializers.createFunctionSerializer().registerTypeHierarchyAdapter(ILootModifierCondition.class, ILootModifierCondition.MODIFIER_CONDITIONS).create();

  /** Additional conditions that can consider the previously generated loot */
  private final ILootModifierCondition[] modifierConditions;
  /** Entry for generating loot */
	private final LootPoolEntryContainer entry;
  /** Functions to apply to the entry, allows adding functions to parented loot entries such as alternatives */
	private final LootItemFunction[] functions;
  /** Functions merged into a single function for ease of use */
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

	protected AddEntryLootModifier(LootItemCondition[] conditionsIn, ILootModifierCondition[] modifierConditions, LootPoolEntryContainer entry, LootItemFunction[] functions) {
		super(conditionsIn);
    this.modifierConditions = modifierConditions;
    this.entry = entry;
		this.functions = functions;
		this.combinedFunctions = LootItemFunctions.compose(functions);
	}

  /** Creates a builder for this loot modifier */
  public static Builder builder(LootPoolEntryContainer entry) {
    return new Builder(entry);
  }

  /** Creates a builder for this loot modifier */
  public static Builder builder(LootPoolEntryContainer.Builder<?> builder) {
    return builder(builder.build());
  }

  @Nonnull
	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
    // if any condition fails, exit immediately
    for (ILootModifierCondition modifierCondition : modifierConditions) {
      if (!modifierCondition.test(generatedLoot, context)) {
        return generatedLoot;
      }
    }
    // generate the actual entry
    Consumer<ItemStack> consumer = LootItemFunction.decorate(this.combinedFunctions, generatedLoot::add, context);
    entry.expand(context, generator -> generator.createItemStack(consumer, context));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<AddEntryLootModifier> {
		@Override
		public AddEntryLootModifier read(ResourceLocation location, JsonObject object, LootItemCondition[] conditions) {
			LootPoolEntryContainer entry = GSON.fromJson(GsonHelper.getAsJsonObject(object, "entry"), LootPoolEntryContainer.class);

      // loot modifier conditions
      ILootModifierCondition[] modifierConditions;
      if (object.has("post_conditions")) {
        modifierConditions = GSON.fromJson(GsonHelper.getAsJsonArray(object, "modifier_conditions"), ILootModifierCondition[].class);
      } else {
        modifierConditions = new ILootModifierCondition[0];
      }

      // functions
      LootItemFunction[] functions;
			if (object.has("functions")) {
				functions = GSON.fromJson(GsonHelper.getAsJsonArray(object, "functions"), LootItemFunction[].class);
			} else {
				functions = new LootItemFunction[0];
			}
			return new AddEntryLootModifier(conditions, modifierConditions, entry, functions);
		}

		@Override
		public JsonObject write(AddEntryLootModifier instance) {
			JsonObject object = makeConditions(instance.conditions);
      if (instance.modifierConditions.length > 0) {
        object.add("modifier_conditions", GSON.toJsonTree(instance.modifierConditions, ILootModifierCondition[].class));
      }
			object.add("entry", GSON.toJsonTree(instance.entry, LootPoolEntryContainer.class));
      if (instance.functions.length > 0) {
        object.add("functions", GSON.toJsonTree(instance.functions, LootItemFunction[].class));
      }
			return object;
		}
	}

  /** Builder for a conditional loot entry */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {

    private final List<ILootModifierCondition> modifierConditions = new ArrayList<>();
    private final LootPoolEntryContainer entry;
    private final List<LootItemFunction> functions = new ArrayList<>();

    /**
     * Adds a loot entry condition to the builder
     */
    public Builder addCondition(ILootModifierCondition condition) {
      modifierConditions.add(condition);
      return this;
    }

    /**
     * Adds a loot function to the builder
     */
    public Builder addFunction(LootItemFunction function) {
      functions.add(function);
      return this;
    }

    @Override
    public void build(String name, GlobalLootModifierProvider provider) {
      provider.add(name, MantleLoot.ADD_ENTRY, new AddEntryLootModifier(getConditions(), modifierConditions.toArray(new ILootModifierCondition[0]), entry, functions.toArray(new LootItemFunction[0])));
    }
  }
}
