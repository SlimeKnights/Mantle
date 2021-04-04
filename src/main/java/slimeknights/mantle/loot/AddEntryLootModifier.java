package slimeknights.mantle.loot;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootEntry;
import net.minecraft.loot.LootSerializers;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.loot.functions.ILootFunction;
import net.minecraft.loot.functions.LootFunctionManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/** Loot modifier to inject an additional loot entry into an existing table */
public class AddEntryLootModifier extends LootModifier {
	private static final Gson GSON = LootSerializers.func_237387_b_().create();

	private final LootEntry entry;
	private final ILootFunction[] functions;
	private final BiFunction<ItemStack, LootContext, ItemStack> combinedFunctions;
	private final boolean requireEmpty;
	protected AddEntryLootModifier(ILootCondition[] conditionsIn, LootEntry entry, ILootFunction[] functions, boolean requireEmpty) {
		super(conditionsIn);
		this.entry = entry;
		this.functions = functions;
		this.combinedFunctions = LootFunctionManager.combine(functions);
		this.requireEmpty = requireEmpty;
	}

	@Nonnull
	@Override
	protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
		if (!requireEmpty || generatedLoot.isEmpty()) {
			Consumer<ItemStack> consumer = ILootFunction.func_215858_a(this.combinedFunctions, generatedLoot::add, context);
			entry.expand(context, generator -> generator.func_216188_a(consumer, context));
		}
		return generatedLoot;
	}

	public static class Serializer extends GlobalLootModifierSerializer<AddEntryLootModifier> {
		@Override
		public AddEntryLootModifier read(ResourceLocation location, JsonObject object, ILootCondition[] conditions) {
			LootEntry entry = GSON.fromJson(JSONUtils.getJsonObject(object, "entry"), LootEntry.class);
			ILootFunction[] functions;
			if (object.has("functions")) {
				functions = GSON.fromJson(JSONUtils.getJsonArray(object, "functions"), ILootFunction[].class);
			} else {
				functions = new ILootFunction[0];
			}
			boolean requireEmpty = JSONUtils.getBoolean(object, "require_empty", false);
			return new AddEntryLootModifier(conditions, entry, functions, requireEmpty);
		}

		@Override
		public JsonObject write(AddEntryLootModifier instance) {
			JsonObject object = makeConditions(instance.conditions);
			object.addProperty("require_empty", instance.requireEmpty);
			object.add("entry", GSON.toJsonTree(instance.entry, LootEntry.class));
			object.add("functions", GSON.toJsonTree(instance.functions, ILootFunction[].class));
			return object;
		}
	}
}
