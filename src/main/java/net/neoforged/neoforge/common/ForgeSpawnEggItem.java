package slimeknights.mantle.compat.neoforged.neoforge.common;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;

import java.util.function.Supplier;

public class ForgeSpawnEggItem extends DeferredSpawnEggItem {
  public ForgeSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int backgroundColor, int highlightColor, Item.Properties props) {
    super(type, backgroundColor, highlightColor, props);
  }

  public static SpawnEggItem fromEntityType(EntityType<?> type) {
    return SpawnEggItem.byId(type);
  }
}
