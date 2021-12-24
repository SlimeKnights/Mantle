package slimeknights.mantle.loot.function;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.item.RetexturedBlockItem;
import slimeknights.mantle.loot.MantleLoot;

import java.util.Set;

/**
 * Applies the data for a retextured block to the dropped item. No configuration needed.
 */
@SuppressWarnings("WeakerAccess")
public class RetexturedLootFunction extends LootItemConditionalFunction {
  public static final Serializer SERIALIZER = new Serializer();

  /**
   * Creates a new instance from the given conditions
   * @param conditions Conditions list
   */
  public RetexturedLootFunction(LootItemCondition[] conditions) {
    super(conditions);
  }

  /** Creates a new instance with no conditions */
  public RetexturedLootFunction() {
    super(new LootItemCondition[0]);
  }

  @Override
  public Set<LootContextParam<?>> getReferencedContextParams() {
    return ImmutableSet.of(LootContextParams.BLOCK_ENTITY);
  }

  @Override
  protected ItemStack run(ItemStack stack, LootContext context) {
    BlockEntity te = context.getParamOrNull(LootContextParams.BLOCK_ENTITY);
    if (te instanceof IRetexturedBlockEntity retextured) {
      RetexturedBlockItem.setTexture(stack, retextured.getTextureName());
    } else {
      String name = te == null ? "null" : te.getClass().getName();
      Mantle.logger.warn("Found wrong tile entity for loot function, expected IRetexturedTileEntity, found {}", name);
    }
    return stack;
  }

  @Override
  public LootItemFunctionType getType() {
    return MantleLoot.RETEXTURED_FUNCTION;
  }

  private static class Serializer extends LootItemConditionalFunction.Serializer<RetexturedLootFunction> {
    @Override
    public RetexturedLootFunction deserialize(JsonObject json, JsonDeserializationContext ctx, LootItemCondition[] conditions) {
      return new RetexturedLootFunction(conditions);
    }
  }
}
