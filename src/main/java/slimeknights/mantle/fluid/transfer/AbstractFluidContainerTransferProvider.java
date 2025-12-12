package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.PackOutput.Target;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.crafting.CraftingHelper;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.FluidObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Data gen for fluid transfer logic */
@SuppressWarnings("unused")
public abstract class AbstractFluidContainerTransferProvider extends GenericDataProvider {
  private final Map<ResourceLocation,TransferJson> allTransfers = new HashMap<>();
  private final String modId;

  public AbstractFluidContainerTransferProvider(PackOutput packOutput, String modId) {
    super(packOutput, Target.DATA_PACK, FluidContainerTransferManager.FOLDER, FluidContainerTransferManager.GSON);
    this.modId = modId;
  }

  /** Function to add all relevant transfers */
  protected abstract void addTransfers();

  /** Adds a transfer to be saved */
  protected void addTransfer(ResourceLocation id, IFluidContainerTransfer transfer, ICondition... conditions) {
    TransferJson previous = allTransfers.putIfAbsent(id, new TransferJson(transfer, conditions));
    if (previous != null) {
      throw new IllegalArgumentException("Duplicate fluid container transfer " + id);
    }
  }

  /** Adds a transfer to be saved */
  protected void addTransfer(String name, IFluidContainerTransfer transfer, ICondition... conditions) {
    addTransfer(new ResourceLocation(modId, name), transfer, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, FluidOutput fill, FluidIngredient drain, boolean nbt, ICondition... conditions) {
    if (nbt) {
      addTransfer(prefix + "empty", new EmptyFluidWithNBTTransfer(Ingredient.of(item), ItemOutput.fromItem(container), fill), conditions);
      addTransfer(prefix + "fill", new FillFluidWithNBTTransfer(Ingredient.of(container), ItemOutput.fromItem(item), drain), conditions);
    } else {
      addTransfer(prefix + "empty", new EmptyFluidContainerTransfer(Ingredient.of(item), ItemOutput.fromItem(container), fill), conditions);
      addTransfer(prefix + "fill", new FillFluidContainerTransfer(Ingredient.of(container), ItemOutput.fromItem(item), drain), conditions);
    }
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, Fluid fluid, TagKey<Fluid> tag, int amount, boolean nbt, ICondition... conditions) {
    addFillEmpty(prefix, item, container, FluidOutput.fromFluid(fluid, amount), FluidIngredient.of(tag, amount), nbt, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, TagKey<Fluid> tag, int amount, boolean nbt, ICondition... conditions) {
    addFillEmpty(prefix, item, container, FluidOutput.fromTag(tag, amount), FluidIngredient.of(tag, amount), nbt, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, FluidObject<?> fluid, int amount, boolean nbt, ICondition... conditions) {
    addFillEmpty(prefix, item, container, fluid.result(amount), fluid.ingredient(amount), nbt, conditions);
  }

  /** @deprecated use {@link #addFillEmpty(String, ItemLike, ItemLike, Fluid, TagKey, int, boolean, ICondition...)} */
  @Deprecated(forRemoval = true)
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, Fluid fluid, TagKey<Fluid> tag, int amount, ICondition... conditions) {
    addFillEmpty(prefix, item, container, fluid, tag, amount, false, conditions);
  }

  /** @deprecated use {@link #addFillEmpty(String, ItemLike, ItemLike, Fluid, TagKey, int, boolean, ICondition...)} */
  @Deprecated(forRemoval = true)
  protected void addFillEmptyNBT(String prefix, ItemLike item, ItemLike container, Fluid fluid, TagKey<Fluid> tag, int amount, ICondition... conditions) {
    addFillEmpty(prefix, item, container, fluid, tag, amount, true, conditions);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    addTransfers();
    return allOf(allTransfers.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().toJson())));
  }

  /** Json with transfer and condition */
  private record TransferJson(IFluidContainerTransfer transfer, ICondition[] conditions) {
    /** Serializes this to JSON */
    public JsonElement toJson() {
      JsonElement element = FluidContainerTransferManager.GSON.toJsonTree(transfer, IFluidContainerTransfer.class);
      assert element.isJsonObject();
      if (conditions.length != 0) {
        JsonArray array = new JsonArray();
        for (ICondition condition : conditions) {
          array.add(CraftingHelper.serialize(condition));
        }
        element.getAsJsonObject().add("conditions", array);
      }
      return element;
    }
  }
}
