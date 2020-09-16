package slimeknights.mantle.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootFunction;
import net.minecraft.loot.LootFunctionType;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.tileentity.TileEntity;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;

import java.util.Set;

/**
 * Applies the data for a retextured block to the dropped item. No configuration needed.
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedLootFunction extends LootFunction {
  /**
   * Creates a new instance from the given conditions
   * @param conditions Conditions list
   */
  public RetexturedLootFunction(ILootCondition[] conditions) {
    super(conditions);
  }

  /** Creates a new instance with no conditions */
  public RetexturedLootFunction() {
    super(new ILootCondition[0]);
  }

  @Override
  public Set<LootParameter<?>> getRequiredParameters() {
    return ImmutableSet.of(LootParameters.BLOCK_ENTITY);
  }

  @Override
  protected ItemStack doApply(ItemStack stack, LootContext context) {
    TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
    if (te instanceof IRetexturedTileEntity) {
      RetexturedBlockItem.setTexture(stack, ((IRetexturedTileEntity)te).getTextureName());
    } else {
      String name = te == null ? "null" : te.getClass().getName();
      Mantle.logger.warn("Found wrong tile entity for loot function, expected IRetexturedTileEntity, found {}", name);
    }
    return stack;
  }

  @Override
  public LootFunctionType getFunctionType() {
    return MantleLoot.RETEXTURED_FUNCTION;
  }

  public static class Serializer extends LootFunction.Serializer<RetexturedLootFunction> {
    @Override
    public RetexturedLootFunction deserialize(JsonObject json, JsonDeserializationContext ctx, ILootCondition[] conditions) {
      return new RetexturedLootFunction(conditions);
    }
  }
}
