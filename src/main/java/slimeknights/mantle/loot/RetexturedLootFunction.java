package slimeknights.mantle.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.ConditionalLootFunction;
import net.minecraft.loot.function.LootFunctionType;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.tileentity.IRetexturedTileEntity;

import java.util.Set;

/**
 * Applies the data for a retextured block to the dropped item. No configuration needed.
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedLootFunction extends ConditionalLootFunction {
  /**
   * Creates a new instance from the given conditions
   * @param conditions Conditions list
   */
  public RetexturedLootFunction(LootCondition[] conditions) {
    super(conditions);
  }

  /** Creates a new instance with no conditions */
  public RetexturedLootFunction() {
    super(new LootCondition[0]);
  }

  @Override
  public Set<LootContextParameter<?>> getRequiredParameters() {
    return ImmutableSet.of(LootContextParameters.BLOCK_ENTITY);
  }

  @Override
  protected ItemStack process(ItemStack stack, LootContext context) {
    BlockEntity te = context.get(LootContextParameters.BLOCK_ENTITY);
    if (te instanceof IRetexturedTileEntity) {
      RetexturedBlockItem.setTexture(stack, ((IRetexturedTileEntity)te).getTextureName());
    } else {
      String name = te == null ? "null" : te.getClass().getName();
      Mantle.logger.warn("Found wrong tile entity for loot function, expected IRetexturedTileEntity, found {}", name);
    }
    return stack;
  }

  @Override
  public LootFunctionType getType() {
    return MantleLoot.RETEXTURED_FUNCTION;
  }

  public static class Serializer extends ConditionalLootFunction.Serializer<RetexturedLootFunction> {
    @Override
    public RetexturedLootFunction fromJson(JsonObject json, JsonDeserializationContext ctx, LootCondition[] conditions) {
      return new RetexturedLootFunction(conditions);
    }
  }
}
