package slimeknights.mantle.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootGsons;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import org.jetbrains.annotations.Nonnull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table */
public class AddEntryLootModifier extends LootModifier {
	private static final Gson GSON = LootGsons.getFunctionGsonBuilder().create();

	private final LootPoolEntry entry;
	private final LootFunction[] functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;
	private final boolean requireEmpty;
	protected AddEntryLootModifier(LootCondition[] conditionsIn, LootPoolEntry entry, LootFunction[] functions, boolean requireEmpty) {
		super(conditionsIn);
		this.entry = entry;
		this.functions = functions;
		this.combinedFunctions = LootFunctionTypes.join(functions);
		this.requireEmpty = requireEmpty;
	}

	@Nonnull
	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!requireEmpty || generatedLoot.isEmpty()) {
			Consumer<ItemStack> consumer = LootFunction.apply(this.combinedFunctions, generatedLoot::add, context);
			entry.expand(context, generator -> generator.generateLoot(consumer, context));
		}
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<AddEntryLootModifier> {
		@Override
		public AddEntryLootModifier read(Identifier location, JsonObject object, LootCondition[] conditions) {
			LootPoolEntry entry = GSON.fromJson(JsonHelper.getObject(object, "entry"), LootPoolEntry.class);
			LootFunction[] functions;
			if (object.has("functions")) {
				functions = GSON.fromJson(JsonHelper.getArray(object, "functions"), LootFunction[].class);
			} else {
				functions = new LootFunction[0];
			}
			boolean requireEmpty = JsonHelper.getBoolean(object, "require_empty", false);
			return new AddEntryLootModifier(conditions, entry, functions, requireEmpty);
		}

		@Override
		public JsonObject write(AddEntryLootModifier instance) {
			JsonObject object = makeConditions(instance.conditions);
			object.addProperty("require_empty", instance.requireEmpty);
			object.add("entry", GSON.toJsonTree(instance.entry, LootPoolEntry.class));
			object.add("functions", GSON.toJsonTree(instance.functions, LootFunction[].class));
			return object;
		}
	}
}
