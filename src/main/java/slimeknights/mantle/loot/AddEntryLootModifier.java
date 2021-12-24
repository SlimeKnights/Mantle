package slimeknights.mantle.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
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
	static final Gson GSON = LootSerializers.createFunctionSerializer().registerTypeHierarchyAdapter(ILootModifierCondition.class, ILootModifierCondition.MODIFIER_CONDITIONS).create();

  /** Additional conditions that can consider the previously generated loot */
  private final ILootModifierCondition[] modifierConditions;
  /** Entry for generating loot */
	private final LootEntry entry;
  /** Functions to apply to the entry, allows adding functions to parented loot entries such as alternatives */
	private final ILootFunction[] functions;
  /** Functions merged into a single function for ease of use */
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

	protected AddEntryLootModifier(ILootCondition[] conditionsIn, ILootModifierCondition[] modifierConditions, LootEntry entry, ILootFunction[] functions) {
		super(conditionsIn);
    this.modifierConditions = modifierConditions;
    this.entry = entry;
		this.functions = functions;
		this.combinedFunctions = LootFunctionManager.compose(functions);
	}

  /** @deprecated use {@link #AddEntryLootModifier(ILootCondition[], ILootModifierCondition[], LootEntry, ILootFunction[])}} */
  @Deprecated
  protected AddEntryLootModifier(ILootCondition[] conditionsIn, LootEntry entry, ILootFunction[] functions, boolean requireEmpty) {
    this(conditionsIn, requireEmpty ? new ILootModifierCondition[]{ EmptyModifierLootCondition.INSTANCE } : new ILootModifierCondition[0], entry, functions);
  }

  /** Creates a builder for this loot modifier */
  public static Builder builder(LootEntry entry) {
    return new Builder(entry);
  }

  /** Creates a builder for this loot modifier */
  public static Builder builder(LootEntry.Builder<?> builder) {
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
    Consumer<ItemStack> consumer = ILootFunction.decorate(this.combinedFunctions, generatedLoot::add, context);
    entry.expand(context, generator -> generator.createItemStack(consumer, context));
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<AddEntryLootModifier> {
		@Override
		public AddEntryLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
			LootEntry entry = GSON.fromJson(JSONUtils.getAsJsonObject(object, "entry"), LootEntry.class);

      // loot modifier conditions
      ILootModifierCondition[] modifierConditions;
      if (object.has("post_conditions")) {
        modifierConditions = GSON.fromJson(JSONUtils.getAsJsonArray(object, "modifier_conditions"), ILootModifierCondition[].class);
      } else {
        modifierConditions = new ILootModifierCondition[0];
      }
      // backwards compat
      if (JSONUtils.getAsBoolean(object, "require_empty", false)) {
        Mantle.logger.warn("Using deprecated Loot Modifier property require_empty, use the mantle:empty post_condition instead");
        modifierConditions = Arrays.copyOf(modifierConditions, modifierConditions.length + 1);
        modifierConditions[modifierConditions.length - 1] = EmptyModifierLootCondition.INSTANCE;
      }
      // functions
      ILootFunction[] functions;
			if (object.has("functions")) {
				functions = GSON.fromJson(JSONUtils.getAsJsonArray(object, "functions"), ILootFunction[].class);
			} else {
				functions = new ILootFunction[0];
			}
			return new AddEntryLootModifier(conditions, modifierConditions, entry, functions);
		}

		@Override
		public JsonObject write(AddEntryLootModifier instance) {
			JsonObject object = makeConditions(instance.conditions);
      if (instance.modifierConditions.length > 0) {
        object.add("modifier_conditions", GSON.toJsonTree(instance.modifierConditions, ILootModifierCondition[].class));
      }
			object.add("entry", GSON.toJsonTree(instance.entry, LootEntry.class));
      if (instance.functions.length > 0) {
        object.add("functions", GSON.toJsonTree(instance.functions, ILootFunction[].class));
      }
			return object;
		}
	}

  /** Builder for a conditional loot entry */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder extends AbstractLootModifierBuilder<Builder> {

    private final List<ILootModifierCondition> modifierConditions = new ArrayList<>();
    private final LootEntry entry;
    private final List<ILootFunction> functions = new ArrayList<>();

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
    public Builder addFunction(ILootFunction function) {
      functions.add(function);
      return this;
    }

    @Override
    public void build(String name, GlobalLootModifierProvider provider) {
      provider.add(name, MantleLoot.ADD_ENTRY, new AddEntryLootModifier(getConditions(), modifierConditions.toArray(new ILootModifierCondition[0]), entry, functions.toArray(new ILootFunction[0])));
    }
  }
}
