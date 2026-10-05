package slimeknights.mantle.fluid.transfer;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.conditions.ICondition;
import slimeknights.mantle.data.GenericRegistryDataProvider;
import slimeknights.mantle.data.loadable.field.ContextKey;
import slimeknights.mantle.recipe.helper.FluidOutput;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.typed.TypedMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/** Data gen for fluid transfer logic */
@SuppressWarnings("unused")
public abstract class AbstractFluidContainerTransferProvider extends GenericRegistryDataProvider {
  private final Map<ResourceLocation,TransferJson> allTransfers = new HashMap<>();
  private final String modId;

  public AbstractFluidContainerTransferProvider(PackOutput packOutput, String modId, CompletableFuture<HolderLookup.Provider> registries) {
    super(packOutput, FluidContainerTransferManager.FOLDER, registries);
    this.modId = modId;
  }

  /** Function to add all relevant transfers */
  protected abstract void addTransfers(HolderLookup.Provider lookup);

  /** Adds a transfer to be saved */
  protected void addTransfer(ResourceLocation id, IFluidContainerTransfer transfer, ICondition... conditions) {
    TransferJson previous = allTransfers.putIfAbsent(id, new TransferJson(transfer, conditions));
    if (previous != null) {
      throw new IllegalArgumentException("Duplicate fluid container transfer " + id);
    }
  }

  /** Adds a transfer to be saved */
  protected void addTransfer(String name, IFluidContainerTransfer transfer, ICondition... conditions) {
    addTransfer(ResourceLocation.fromNamespaceAndPath(modId, name), transfer, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, FluidOutput fill, FluidIngredient drain, List<DataComponentType<?>> copy, ICondition... conditions) {
    if (!copy.isEmpty()) {
      addTransfer(prefix + "empty", new EmptyFluidCopyDataTransfer(Ingredient.of(item), ItemOutput.fromItem(container), fill, copy), conditions);
      addTransfer(prefix + "fill", new FillFluidCopyDataTransfer(Ingredient.of(container), ItemOutput.fromItem(item), drain, copy), conditions);
    } else {
      addTransfer(prefix + "empty", new EmptyFluidContainerTransfer(Ingredient.of(item), ItemOutput.fromItem(container), fill), conditions);
      addTransfer(prefix + "fill", new FillFluidContainerTransfer(Ingredient.of(container), ItemOutput.fromItem(item), drain), conditions);
    }
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, Fluid fluid, TagKey<Fluid> tag, int amount, List<DataComponentType<?>> copy, ICondition... conditions) {
    addFillEmpty(prefix, item, container, FluidOutput.fromFluid(fluid, amount), FluidIngredient.of(tag, amount), copy, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, TagKey<Fluid> tag, int amount, List<DataComponentType<?>> copy, ICondition... conditions) {
    addFillEmpty(prefix, item, container, FluidOutput.fromTag(tag, amount), FluidIngredient.of(tag, amount), copy, conditions);
  }

  /** Adds generic fill and empty for a container */
  protected void addFillEmpty(String prefix, ItemLike item, ItemLike container, FluidObject<?> fluid, int amount, List<DataComponentType<?>> copy, ICondition... conditions) {
    addFillEmpty(prefix, item, container, fluid.result(amount), fluid.ingredient(amount), copy, conditions);
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache, HolderLookup.Provider lookup) {
    addTransfers(lookup);
    TypedMap context = ContextKey.registryContext(lookup);
    return allOf(allTransfers.entrySet().stream().map(entry -> saveJson(cache, entry.getKey(), entry.getValue().toJson(context))));
  }

  /** Json with transfer and condition */
  private record TransferJson(IFluidContainerTransfer transfer, ICondition[] conditions) {
    /** Serializes this to JSON */
    public JsonObject toJson(TypedMap context) {
      JsonObject json = new JsonObject();
      IFluidContainerTransfer.LOADER.serializeInto(transfer, json);
      if (conditions.length != 0) {
        json.add("conditions", JsonHelper.serializeArray(ICondition.CODEC, conditions));
      }
      return json;
    }
  }
}
