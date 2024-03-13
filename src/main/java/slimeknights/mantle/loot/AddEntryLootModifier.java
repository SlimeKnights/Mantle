package slimeknights.mantle.loot;

import com.google.gson.Gson;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.Deserializers;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import slimeknights.mantle.data.MantleCodecs;
import slimeknights.mantle.loot.condition.ILootModifierCondition;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table */
public class AddEntryLootModifier extends LootModifier {
  public static final Codec<AddEntryLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).and(inst.group(
    ILootModifierCondition.CODEC.listOf().fieldOf("post_conditions").forGetter(m -> m.modifierConditions),
    MantleCodecs.LOOT_ENTRY.fieldOf("entry").forGetter(m -> m.entry),
    MantleCodecs.LOOT_FUNCTIONS.fieldOf("functions").forGetter(m -> m.functions))).apply(inst, AddEntryLootModifier::new));

	static final Gson GSON = Deserializers.createFunctionSerializer().registerTypeHierarchyAdapter(ILootModifierCondition.class, ILootModifierCondition.MODIFIER_CONDITIONS).create();

  /** Additional conditions that can consider the previously generated loot */
  private final List<ILootModifierCondition> modifierConditions;
  /** Entry for generating loot */
	private final LootPoolEntryContainer entry;
  /** Functions to apply to the entry, allows adding functions to parented loot entries such as alternatives */
	private final LootItemFunction[] functions;
  /** Functions merged into a single function for ease of use */
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;

	protected AddEntryLootModifier(LootItemCondition[] conditionsIn, List<ILootModifierCondition> modifierConditions, LootPoolEntryContainer entry, LootItemFunction[] functions) {
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
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
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

  @Override
  public Codec<? extends IGlobalLootModifier> codec() {
    return CODEC;
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

    /** Builds the final modifier */
    public AddEntryLootModifier build() {
      return new AddEntryLootModifier(getConditions(), modifierConditions, entry, functions.toArray(new LootItemFunction[0]));
    }
  }
}
